package com.kazuki43zoo.api;

import com.kazuki43zoo.component.DownloadSupport;
import com.kazuki43zoo.domain.model.MockApiResponse;
import com.kazuki43zoo.domain.service.MockApiResponseService;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@RestController
public class ApiStubRestController {

    private static final String API_PREFIX_PATH = "/api";
    private static final String HEADER_SEPARATOR = "\r\n";
    private static final String HEADER_KEY_VALUE_SEPARATOR = ":";

    @Autowired
    MockApiResponseService mockApiResponseService;

    @Autowired
    DownloadSupport downloadSupport;

    @Autowired
    ApiStubProperties apiStubProperties;

    @Autowired
    ApiEvidenceFactory apiEvidenceFactory;

    @Autowired(required = false)
    Clock clock = Clock.systemDefaultZone();

    @RequestMapping(path = API_PREFIX_PATH + "/**")
    public ResponseEntity<Object> handleApiRequest(HttpServletRequest request,
                                                   RequestEntity<InputStreamResource> requestEntity)
            throws IOException, ServletException, InterruptedException {

        final String correlationId = Optional.ofNullable(request.getHeader(apiStubProperties.getCorrelationIdKey()))
                .orElse(UUID.randomUUID().toString());

        MDC.put(apiStubProperties.getCorrelationIdKey(), correlationId);

        final ApiEvidence evidence = apiEvidenceFactory.create(request, correlationId);

        final Logger logger = evidence.getLogger();

        try {

            evidence.start();
            evidence.request(request, requestEntity);

            final String path = request.getServletPath().replace(API_PREFIX_PATH, "");
            final String method = request.getMethod();

            MockApiResponse mockResponse = mockApiResponseService.find(path, method);

            if (mockResponse.getId() == 0) {
                logger.warn("Mock Response is not found.");
            }

            // Status Code
            Integer statusCode = Optional.ofNullable(mockResponse.getStatusCode())
                    .orElse(HttpStatus.OK.value());

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
            headers.add(apiStubProperties.getCorrelationIdKey(), correlationId);

            // Http Body
            Object body = Optional.ofNullable(mockResponse.getBody())
                    .map(InputStreamResource::new).orElse(
                            Optional.ofNullable(mockResponse.getAttachmentFile())
                                    .map(InputStreamResource::new).orElse(null));

            ResponseEntity<Object> responseEntity =
                    ResponseEntity.status(statusCode).headers(headers).body(body);

            // Wait processing
            if (mockResponse.getWaitingMsec() != null && mockResponse.getWaitingMsec() > 0) {
                logger.info("Waiting {} msec.", mockResponse.getWaitingMsec());
                TimeUnit.MILLISECONDS.sleep(mockResponse.getWaitingMsec());
            }

            evidence.response(responseEntity);
            evidence.end();

            return responseEntity;

        } finally {
            MDC.clear();
        }
    }

}
