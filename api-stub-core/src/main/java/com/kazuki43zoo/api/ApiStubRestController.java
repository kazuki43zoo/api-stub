package com.kazuki43zoo.api;

import com.kazuki43zoo.component.DownloadSupport;
import com.kazuki43zoo.domain.model.MockApiResponse;
import com.kazuki43zoo.domain.service.MockApiResponseService;
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
class ApiStubRestController {

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

        final ApiEvidence evidence = apiEvidenceFactory.create(request, correlationId);

        try {

            evidence.start();
            evidence.request(request, requestEntity);

            final String path = request.getServletPath().replace(API_PREFIX_PATH, "");
            final String method = request.getMethod();

            MockApiResponse mockApiResponse = mockApiResponseService.find(path, method);

            if (mockApiResponse.getId() == 0) {
                evidence.warn("Mock Response is not found.");
            }

            // Status Code
            Integer statusCode = Optional.ofNullable(mockApiResponse.getStatusCode())
                    .orElse(HttpStatus.OK.value());

            // Http Headers
            HttpHeaders headers = new HttpHeaders();
            if (StringUtils.hasLength(mockApiResponse.getHeader())) {
                Stream.of(mockApiResponse.getHeader().split(HEADER_SEPARATOR)).forEach(e -> {
                    String[] headerElements = e.split(HEADER_KEY_VALUE_SEPARATOR);
                    headers.add(headerElements[0].trim(), headerElements[1].trim());
                });
            }
            if (StringUtils.hasLength(mockApiResponse.getFileName())
                    && !headers.containsKey(HttpHeaders.CONTENT_DISPOSITION)) {
                downloadSupport.addContentDisposition(headers, mockApiResponse.getFileName());
            }
            headers.add(apiStubProperties.getCorrelationIdKey(), correlationId);

            // Http Body
            Object body = Optional.ofNullable(mockApiResponse.getBody())
                    .map(InputStreamResource::new).orElse(
                            Optional.ofNullable(mockApiResponse.getAttachmentFile())
                                    .map(InputStreamResource::new).orElse(null));

            // Wait processing
            if (mockApiResponse.getWaitingMsec() != null && mockApiResponse.getWaitingMsec() > 0) {
                evidence.info("Waiting {} msec.", mockApiResponse.getWaitingMsec());
                TimeUnit.MILLISECONDS.sleep(mockApiResponse.getWaitingMsec());
            }

            // Create Response Entity
            ResponseEntity<Object> responseEntity =
                    ResponseEntity.status(statusCode).headers(headers).body(body);

            evidence.response(responseEntity);

            return responseEntity;

        } finally {
            evidence.end();
        }

    }

}
