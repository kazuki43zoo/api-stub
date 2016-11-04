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
package com.kazuki43zoo.api.handler;

import com.kazuki43zoo.api.ApiEvidence;
import com.kazuki43zoo.component.web.DownloadSupport;
import com.kazuki43zoo.config.ApiStubProperties;
import com.kazuki43zoo.domain.model.ApiResponse;
import com.kazuki43zoo.domain.service.ApiResponseService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class MockResponseHandler {

    private static final String HEADER_SEPARATOR = "\r\n";
    private static final String HEADER_KEY_VALUE_SEPARATOR = ":";

    private final ApiResponseService apiResponseService;
    private final DownloadSupport downloadSupport;
    private final ApiStubProperties properties;

    public MockResponseHandler(ApiResponseService apiResponseService, DownloadSupport downloadSupport, ApiStubProperties properties) {
        this.apiResponseService = apiResponseService;
        this.downloadSupport = downloadSupport;
        this.properties = properties;
    }

    public ResponseEntity<Object> perform(String path, String method, String dataKey, ApiEvidence evidence) throws UnsupportedEncodingException, InterruptedException {

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
                .map(InputStreamResource::new)
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


}
