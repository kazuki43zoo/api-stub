package com.kazuki43zoo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazuki43zoo.api.key.KeyExtractor;
import com.kazuki43zoo.component.web.DownloadSupport;
import com.kazuki43zoo.domain.model.Api;
import com.kazuki43zoo.domain.model.ApiResponse;
import com.kazuki43zoo.domain.service.ApiResponseService;
import com.kazuki43zoo.domain.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
class ApiStubRestController {

    private static final String API_PREFIX_PATH = "/api";
    private static final String HEADER_SEPARATOR = "\r\n";
    private static final String HEADER_KEY_VALUE_SEPARATOR = ":";

    @Autowired
    ApiResponseService apiResponseService;

    @Autowired
    ApiService apiService;

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

    @RequestMapping(path = API_PREFIX_PATH + "/**")
    public ResponseEntity<InputStreamResource> handleApiRequest(HttpServletRequest request,
                                                                RequestEntity<String> requestEntity)
            throws IOException, ServletException, InterruptedException {

        final String correlationId = Optional.ofNullable(request.getHeader(properties.getCorrelationIdKey()))
                .orElse(UUID.randomUUID().toString());

        final String path = request.getServletPath().replace(API_PREFIX_PATH, "");
        final String method = request.getMethod();

        String dataKey = extractKey(path, method, request, requestEntity);

        final ApiEvidence evidence = apiEvidenceFactory.create(request, dataKey, correlationId);

        try {

            evidence.start();
            evidence.request(request, requestEntity);

            final ApiResponse apiResponse = apiResponseService.findOne(path, method, dataKey);

            if (apiResponse.getId() == 0) {
                evidence.warn("Mock Response is not found.");
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
            if (StringUtils.hasLength(apiResponse.getFileName())
                    && !responseHeaders.containsKey(HttpHeaders.CONTENT_DISPOSITION)) {
                downloadSupport.addContentDisposition(responseHeaders, apiResponse.getFileName());
            }
            responseHeaders.add(properties.getCorrelationIdKey(), correlationId);

            // Response Body
            final InputStreamResource responseBody = Optional.ofNullable(apiResponse.getBody())
                    .map(InputStreamResource::new).orElse(Optional.ofNullable(apiResponse.getAttachmentFile())
                            .map(InputStreamResource::new).orElse(null));

            // Wait processing
            if (apiResponse.getWaitingMsec() != null && apiResponse.getWaitingMsec() > 0) {
                evidence.info("Waiting {} msec.", apiResponse.getWaitingMsec());
                TimeUnit.MILLISECONDS.sleep(apiResponse.getWaitingMsec());
            }

            // Response Entity
            final ResponseEntity<InputStreamResource> responseEntity =
                    ResponseEntity.status(statusCode).headers(responseHeaders).body(responseBody);

            evidence.response(responseEntity);

            return responseEntity;

        } finally {
            evidence.end();
        }

    }

    private String extractKey(String path, String method, HttpServletRequest request, RequestEntity<String> requestEntity) throws IOException {
        Api api = apiService.findOne(path, method);
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
                .toArray(new String[]{});
        String key = null;
        try {
            List<String> keys = keyExtractor.extract(request, requestEntity.getBody(), expressions);
            key = api.getKeyGeneratingStrategy().generate(keys);
        } catch (Exception e) {/*Skip*/}
        return key;
    }

}
