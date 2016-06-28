package com.kazuki43zoo.api;

import com.kazuki43zoo.component.DownloadSupport;
import com.kazuki43zoo.domain.MockApi;
import com.kazuki43zoo.domain.MockApiResponse;
import com.kazuki43zoo.service.MockApiResponseService;
import com.kazuki43zoo.service.MockApiService;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
public class ApiStubRestController {

    private static final String API_PREFIX_PATH = "/api";
    private static final String HEADER_SEPARATOR = "\r\n";
    private static final String HEADER_KEY_VALUE_SEPARATOR = ":";

    @Autowired
    MockApiService mockApiService;

    @Autowired
    MockApiResponseService mockApiResponseService;

    @Autowired
    DownloadSupport downloadSupport;

    @Autowired
    ContentNegotiationManager contentNegotiationManager;

    @Autowired
    ApiStubProperties apiStubProperties;

    @RequestMapping(path = API_PREFIX_PATH + "/**")
    public ResponseEntity<Object> handleApiRequest(HttpServletRequest request,
                                                   RequestEntity<InputStreamResource> requestEntity)
            throws IOException, ServletException {

        final String correlationId = Optional.ofNullable(request.getHeader(apiStubProperties.getCorrelationIdKey()))
                .orElse(UUID.randomUUID().toString());
        MDC.put(apiStubProperties.getCorrelationIdKey(), correlationId);

        final String path = request.getServletPath().replace(API_PREFIX_PATH, "");
        final String method = request.getMethod();

        String contentExtension = contentNegotiationManager.resolveFileExtensions(MediaType.parseMediaType(request.getContentType()))
                .stream().findFirst().orElse("txt");
        final ApiEvidence evidence = new ApiEvidence(apiStubProperties, method, request.getServletPath(), correlationId, contentExtension);

        final Logger logger = evidence.getLogger();

        try {

            evidence.start();

            evidence.request(request, requestEntity);

            MockApiResponse mockResponse = mockApiResponseService.findOne(path, method);
            MockApi mockApi = mockApiService.findOneBy(path, method);

            // Status Code
            Integer statusCode = HttpStatus.OK.value();
            if (mockResponse.getStatusCode() != null) {
                statusCode = mockResponse.getStatusCode();
            }

            // Http Headers
            HttpHeaders headers = new HttpHeaders();
            if (StringUtils.hasLength(mockResponse.getHeader())) {
                Stream.of(mockResponse.getHeader().split(HEADER_SEPARATOR)).forEach(e -> {
                    String[] headerElements = e.split(HEADER_KEY_VALUE_SEPARATOR);
                    headers.add(headerElements[0].trim(), headerElements[1].trim());
                });
            }
            if (StringUtils.hasLength(mockResponse.getFileName())
                    && !headers.containsKey(HttpHeaders.CONTENT_DISPOSITION)) {
                downloadSupport.addContentDisposition(headers, mockResponse.getFileName());
            }
            if (mockApi != null) {
                if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)
                        && StringUtils.hasLength(mockApi.getContentType())) {
                    headers.setContentType(MediaType.parseMediaType(mockApi.getContentType()));
                }
            }
            headers.add(apiStubProperties.getCorrelationIdKey(), correlationId);

            // Http Body
            Object body = null;
            if (mockResponse.getBody() != null) {
                body = new InputStreamResource(mockResponse.getBody());
            }

            ResponseEntity<Object> responseEntity =
                    ResponseEntity.status(statusCode).headers(headers).body(body);

            evidence.response(responseEntity);

            if (mockResponse.getId() == 0) {
                logger.warn("Mock Response is not found.");
            }

            evidence.end();

            return responseEntity;

        } finally {
            MDC.clear();
        }
    }

}
