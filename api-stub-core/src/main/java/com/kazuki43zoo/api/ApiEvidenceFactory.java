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

import com.kazuki43zoo.config.ApiStubProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.accept.ContentNegotiationManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Component
class ApiEvidenceFactory {

    @Autowired
    ApiStubProperties properties;

    @Autowired
    ContentNegotiationManager contentNegotiationManager;


    ApiEvidence create(HttpServletRequest request, String dataKey) {

        final String correlationId = Optional.ofNullable(request.getHeader(properties.getCorrelationIdKey()))
                .orElse(UUID.randomUUID().toString());

        final String contentExtension = Optional.ofNullable(request.getContentType())
                .map(contentType -> contentNegotiationManager.resolveFileExtensions(MediaType.parseMediaType(contentType)))
                .orElseGet(ArrayList::new).stream().findFirst().orElse("txt");

        return new ApiEvidence(properties, request.getMethod(), request.getServletPath(), dataKey, correlationId, contentExtension);
    }

}
