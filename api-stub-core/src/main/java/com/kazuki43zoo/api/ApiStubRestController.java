/**
 *    Copyright 2016 the original author or authors.
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
package com.kazuki43zoo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazuki43zoo.api.key.KeyExtractor;
import com.kazuki43zoo.component.web.DownloadSupport;
import com.kazuki43zoo.config.ApiStubProperties;
import com.kazuki43zoo.domain.model.Api;
import com.kazuki43zoo.domain.model.ApiProxy;
import com.kazuki43zoo.domain.model.ApiResponse;
import com.kazuki43zoo.domain.service.ApiResponseService;
import com.kazuki43zoo.domain.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
class ApiStubRestController {

    private static final String HEADER_SEPARATOR = "\r\n";
    private static final String HEADER_KEY_VALUE_SEPARATOR = ":";

    private final RestOperations restOperations;

    @Autowired
    ApiService apiService;

    @Autowired
    ApiResponseService apiResponseService;

    @Autowired
    DownloadSupport downloadSupport;

    @Autowired
    ApiStubProperties properties;

    @Autowired
    ApiEvidenceFactory apiEvidenceFactory;

    @Autowired(required = false)
    Map<String, KeyExtractor> keyExtractorMap;

    @Autowired
    ObjectMapper jsonObjectMapper;

    ApiStubRestController(RestTemplateBuilder restTemplateBuilder) {
        this.restOperations = restTemplateBuilder.build();
    }

    @RequestMapping(path = "${api.root-path:/api}/**")
    public ResponseEntity<Object> handleApiRequest(HttpServletRequest request, RequestEntity<String> requestEntity)
            throws IOException, ServletException, InterruptedException {

        final String path = request.getServletPath().replace(properties.getRootPath(), "");
        final String method = request.getMethod();

        final Api api = apiService.findOne(path, method);
        final String dataKey = extractKey(api, request, requestEntity);

        final ApiEvidence evidence = apiEvidenceFactory.create(request, dataKey);

        try {

            evidence.start();
            evidence.request(request, requestEntity);

            final ResponseEntity<Object> responseEntity;

            boolean enabledProxy = Optional.ofNullable(api).map(Api::getProxy).map(ApiProxy::getEnabled)
                    .orElseGet(() -> properties.getProxy().isDefaultEnabled());
            if (enabledProxy) {
                responseEntity = doProxy(request, requestEntity, path, method, dataKey, api, evidence);

            } else {
                responseEntity = getMockedResponse(path, method, dataKey, evidence);

            }

            evidence.response(responseEntity);

            return responseEntity;

        } finally {
            evidence.end();
        }

    }

    private String extractKey(Api api, HttpServletRequest request, RequestEntity<String> requestEntity) throws IOException {
        if (api == null) {
            return null;
        }
        KeyExtractor keyExtractor = keyExtractorMap.get(api.getKeyExtractor());
        if (keyExtractor == null || api.getExpressions() == null || api.getKeyGeneratingStrategy() == null) {
            return null;
        }
        String[] expressions = Stream.of(jsonObjectMapper.readValue(api.getExpressions(), String[].class))
                .filter(StringUtils::hasLength)
                .collect(Collectors.toList())
                .toArray(new String[0]);
        String key = null;
        try {
            List<String> keys = keyExtractor.extract(request, requestEntity.getBody(), expressions);
            key = api.getKeyGeneratingStrategy().generate(keys);
        } catch (Exception e) {
            // ignore
            log.debug(e.getMessage(), e);
        }
        return key;
    }

    private ResponseEntity<Object> doProxy(HttpServletRequest request, RequestEntity<String> requestEntity, String path, String method, String dataKey, Api api, ApiEvidence evidence) throws UnsupportedEncodingException {

        final String url = Optional.ofNullable(api).map(Api::getProxy).map(ApiProxy::getUrl).filter(StringUtils::hasLength)
                .orElse(properties.getProxy().getDefaultUrl()) + path + (StringUtils.hasLength(request.getQueryString()) ? "?" + request.getQueryString() : "");


        final RequestEntity.BodyBuilder requestBodyBuilder = RequestEntity.method(HttpMethod.valueOf(method.toUpperCase()), URI.create(url));
        Collections.list(request.getHeaderNames())
                .forEach(headerName -> requestBodyBuilder.header(headerName, Collections.list(request.getHeaders(headerName)).toArray(new String[0])));

        evidence.info("Proxy to {}", url);

        final ResponseEntity<byte[]> proxyResponseEntity =
                restOperations.exchange(requestBodyBuilder.body(requestEntity.getBody()), byte[].class);

        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(proxyResponseEntity.getHeaders());

        final Object body;
        if (responseHeaders.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
            responseHeaders.remove(HttpHeaders.TRANSFER_ENCODING);
            body = new InputStreamResource(new ByteArrayInputStream(proxyResponseEntity.getBody()));
        } else {
            body = proxyResponseEntity.getBody();
        }

        boolean enabledCapturing = Optional.ofNullable(api).map(Api::getProxy).map(ApiProxy::getCapturing)
                .orElseGet(() -> properties.getProxy().isDefaultCapturing());
        if (enabledCapturing) {
            doCapture(path, method, dataKey, proxyResponseEntity, responseHeaders, evidence);
        }

        return ResponseEntity.status(proxyResponseEntity.getStatusCodeValue()).headers(responseHeaders).body(body);
    }

    private ResponseEntity<Object> getMockedResponse(String path, String method, String dataKey, ApiEvidence evidence) throws UnsupportedEncodingException, InterruptedException {

        final ApiResponse apiResponse = apiResponseService.findOne(path, method, dataKey);

        if (apiResponse.getId() == 0) {
            evidence.warn("Mock Response is not found.");
        } else {
            evidence.info("Mock Response is {}.", apiResponse.getId());
        }

        // Status Code
        final Integer statusCode = Optional.ofNullable(apiResponse.getStatusCode())
                .orElse(HttpStatus.OK.value());

        // Response Headers
        final HttpHeaders responseHeaders = new HttpHeaders();
        if (StringUtils.hasLength(apiResponse.getHeader())) {
            Stream.of(apiResponse.getHeader().split(HEADER_SEPARATOR)).forEach(e -> {
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
                .map(InputStreamResource::new).orElse(Optional.ofNullable(apiResponse.getAttachmentFile())
                        .map(InputStreamResource::new).orElse(null));

        // Wait processing
        if (apiResponse.getWaitingMsec() != null && apiResponse.getWaitingMsec() > 0) {
            evidence.info("Waiting {} msec.", apiResponse.getWaitingMsec());
            TimeUnit.MILLISECONDS.sleep(apiResponse.getWaitingMsec());
        }

        return ResponseEntity.status(statusCode).headers(responseHeaders).body(responseBody);
    }

    private void doCapture(String path, String method, String dataKey, ResponseEntity<byte[]> proxyResponseEntity, HttpHeaders responseHeaders, ApiEvidence evidence) throws UnsupportedEncodingException {
        final ApiResponse apiResponse = new ApiResponse();
        apiResponse.setPath(path);
        apiResponse.setMethod(method);
        apiResponse.setDataKey(dataKey);
        apiResponse.setStatusCode(proxyResponseEntity.getStatusCodeValue());
        final StringJoiner headersJoiner = new StringJoiner(HEADER_SEPARATOR);
        responseHeaders.entrySet().forEach(e -> e.getValue().forEach(value -> headersJoiner.add(e.getKey() + HEADER_KEY_VALUE_SEPARATOR + " " + value)));
        apiResponse.setHeader(headersJoiner.toString());
        apiResponse.setFileName(downloadSupport.extractDownloadFileName(responseHeaders));
        if (apiResponse.getFileName() != null) {
            apiResponse.setAttachmentFile(new ByteArrayInputStream(proxyResponseEntity.getBody()));
        } else {
            apiResponse.setBody(new ByteArrayInputStream(proxyResponseEntity.getBody()));
        }
        apiResponseService.createProxyResponse(apiResponse);

        evidence.info("Saved a proxy response into api_proxy_response. id = {}", apiResponse.getId());

    }

}
