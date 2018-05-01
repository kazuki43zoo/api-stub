/*
 *    Copyright 2016-2018 the original author or authors.
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

import com.kazuki43zoo.config.ApiStubProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.accept.ContentNegotiationManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiEvidenceFactory {

    private final ApiStubProperties properties;
    private final ContentNegotiationManager contentNegotiationManager;

    public ApiEvidence create(HttpServletRequest request, String dataKey, String correlationId) {

        final String contentExtension = Optional.ofNullable(request.getContentType())
                .map(contentType -> contentNegotiationManager.resolveFileExtensions(MediaType.parseMediaType(contentType)))
                .orElseGet(ArrayList::new)
                .stream()
                .findFirst()
                .orElse("txt");

        return new ApiEvidence(properties, request.getMethod(), request.getServletPath(), dataKey, correlationId, contentExtension);
    }

}
