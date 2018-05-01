/*
 *    Copyright 2016-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.kazuki43zoo.api.handler;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.kazuki43zoo.api.ApiEvidence;
import com.kazuki43zoo.component.download.DownloadSupport;
import com.kazuki43zoo.config.ApiStubProperties;
import com.kazuki43zoo.domain.model.ApiResponse;
import com.kazuki43zoo.domain.service.ApiResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebExpressionContext;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.expression.ThymeleafEvaluationContext;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class MockResponseHandler {

    private static final String HEADER_SEPARATOR = "\r\n";
    private static final String HEADER_KEY_VALUE_SEPARATOR = ":";

    private final ApplicationContext applicationContext;
    private final ApiResponseService apiResponseService;
    private final DownloadSupport downloadSupport;
    private final ApiStubProperties properties;
    private final Set<IDialect> dialects;
    private ITemplateEngine templateEngine;


    @PostConstruct
    public void setupTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setMessageSource(applicationContext);
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(properties.getResponse().getTemplate().getMode());
        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.setAdditionalDialects(dialects);
        templateEngine.setEnableSpringELCompiler(properties.getResponse().getTemplate().isEnabledSpelCompiler());
        this.templateEngine = templateEngine;
    }

    public ResponseEntity<Object> perform(
            String path,
            String method,
            String dataKey,
            RequestEntity<String> requestEntity,
            HttpServletRequest request,
            HttpServletResponse response,
            ApiEvidence evidence) throws UnsupportedEncodingException, InterruptedException {

        final ApiResponse apiResponse = apiResponseService.findOne(path, method, dataKey);

        final Integer statusCode;
        if (apiResponse.getId() == 0) {
            evidence.warn("Mock Response is not found.");
            statusCode = Optional.ofNullable(properties.getResponse().getHttpStatusForMockNotFound())
                        .orElse(HttpStatus.OK).value();
        } else {
            evidence.info("Mock Response is {}.", apiResponse.getId());
            statusCode = Optional.ofNullable(apiResponse.getStatusCode())
                         .orElse(HttpStatus.OK.value());
        }

        final IWebContext templateContext = createTemplateWebContext(requestEntity, request, response);

        // Response Headers
        final HttpHeaders responseHeaders = new HttpHeaders();
        if (StringUtils.hasLength(apiResponse.getHeader())) {
            String header = processTemplate(apiResponse.getHeader(), templateContext, responseHeaders, evidence);
            Stream.of(header.split(HEADER_SEPARATOR)).filter(e -> e.contains(HEADER_KEY_VALUE_SEPARATOR)).forEach(e -> {
                String[] headerElements = e.split(HEADER_KEY_VALUE_SEPARATOR);
                responseHeaders.add(headerElements[0].trim(), headerElements[1].trim());
            });
        }
        if (StringUtils.hasLength(apiResponse.getFileName())) {
            if (!responseHeaders.containsKey(HttpHeaders.CONTENT_DISPOSITION)) {
                downloadSupport.addContentDisposition(responseHeaders, apiResponse.getFileName());
            }
            if (!responseHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
                responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
        }
        responseHeaders.add(properties.getCorrelationIdKey(), evidence.getCorrelationId());

        // Response Body
        final InputStreamResource responseBody = Optional.ofNullable(apiResponse.getBody())
                .map(body -> {
                    Charset responseCharset = Optional.ofNullable(responseHeaders.getContentType())
                            .map(MediaType::getCharset).orElse(StandardCharsets.UTF_8);
                    return processTemplate(body, responseCharset, templateContext, responseHeaders, evidence);
                })
                .orElse(Optional.ofNullable(apiResponse.getAttachmentFile())
                        .map(InputStreamResource::new)
                        .orElse(null));

        // Wait processing
        if (Optional.ofNullable(apiResponse.getWaitingMsec()).filter(value -> value > 0).isPresent()) {
            evidence.info("Waiting {} msec.", apiResponse.getWaitingMsec());
            TimeUnit.MILLISECONDS.sleep(apiResponse.getWaitingMsec());
        }

        return ResponseEntity.status(statusCode)
                .headers(responseHeaders)
                .body(responseBody);
    }

    private InputStreamResource processTemplate(InputStream body, Charset responseCharset, IWebContext context, HttpHeaders responseHeaders, ApiEvidence evidence) {

        if (properties.getResponse().getTemplate().isDisabled()) {
            return new InputStreamResource(body);
        }

        String template;
        try {
            template = StreamUtils.copyToString(body, responseCharset);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        String result = processTemplate(template, context, responseHeaders, evidence);
        return new InputStreamResource(new ByteArrayInputStream(result.getBytes(responseCharset)));
    }

    private String processTemplate(String template, IWebContext templateContext, HttpHeaders responseHeaders, ApiEvidence evidence) {
        String result;
        try {
            result = templateEngine.process(template, templateContext);
        } catch (Exception e) {
            evidence.error("Return the template body(original body) because template is wrong. template = \r\n" + template , e);
            responseHeaders.add("x-error-code", "template_parsing_error");
            result = template;
        }
        return result;
    }

    private IWebContext createTemplateWebContext(RequestEntity<String> requestEntity, HttpServletRequest request, HttpServletResponse response) {
        ModelMap model = new ModelMap();
        model.addAttribute(requestEntity);
        Optional.ofNullable(requestEntity.getHeaders().getContentType())
                .map(MediaType::toString).map(String::toLowerCase)
                .ifPresent(contentType -> {
                    if (contentType.contains("json")) {
                        model.addAttribute(new RequestJson(requestEntity.getBody()));
                    } else if (contentType.contains("xml")){
                        model.addAttribute(new RequestXml(requestEntity.getBody(), requestEntity.getHeaders().getContentType().getCharset()));
                    }
                });

        final ConversionService conversionService = (ConversionService) request
                .getAttribute(ConversionService.class.getName());
        final ThymeleafEvaluationContext evaluationContext = new ThymeleafEvaluationContext(applicationContext,
                conversionService);
        model.put(ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME, evaluationContext);

        return new WebExpressionContext(templateEngine.getConfiguration(), request, response,
                request.getServletContext(), request.getLocale(), model);
    }



    public static class RequestJson {
        private final Supplier<DocumentContext> documentContextSupplier;
        private DocumentContext documentContext;
        RequestJson(String body) {
            this.documentContextSupplier = () -> {
                if (documentContext == null) {
                    this.documentContext = JsonPath.parse(body);
                }
                return documentContext;
            };
        }
        public Object read(String expression) {
            return documentContextSupplier.get().read(expression);
        }
    }

    public static class RequestXml {
        private final XPath xpath;
        private final Supplier<Document> documentSupplier;
        private Document document;
        RequestXml(String body, Charset charset) {
            this.xpath = XPathFactory.newInstance().newXPath();
            this.documentSupplier = () -> {
                if (document == null) {
                    try {
                        this.document = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder().parse(new ByteArrayInputStream(body.getBytes(Optional.ofNullable(charset).orElse(StandardCharsets.UTF_8))));
                    } catch (SAXException | IOException | ParserConfigurationException e) {
                        throw new IllegalStateException(e);
                    }
                }
                return document;
            };
        }
        public Object read(String expression) throws XPathExpressionException {
            XPathExpression xPathExpression = xpath.compile(expression);
            return xPathExpression.evaluate(documentSupplier.get());
        }
    }

}
