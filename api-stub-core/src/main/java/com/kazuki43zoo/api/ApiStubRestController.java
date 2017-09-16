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
package com.kazuki43zoo.api;

import com.kazuki43zoo.api.handler.MockResponseHandler;
import com.kazuki43zoo.api.handler.ProxyHandler;
import com.kazuki43zoo.api.key.DataKeyExtractor;
import com.kazuki43zoo.config.ApiStubProperties;
import com.kazuki43zoo.domain.model.Api;
import com.kazuki43zoo.domain.model.ApiProxy;
import com.kazuki43zoo.domain.service.ApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
class ApiStubRestController {

    private final ApiService apiService;
    private final ApiStubProperties properties;
    private final ApiEvidenceFactory apiEvidenceFactory;
    private final MockResponseHandler mockResponseHandler;
    private final ProxyHandler proxyHandler;
    private final DataKeyExtractor dataKeyExtractor;

    @RequestMapping(path = "${api.root-path:/api}/**")
    public ResponseEntity<Object> handleApiRequest(HttpServletRequest request, HttpServletResponse response, RequestEntity<String> requestEntity)
            throws IOException, ServletException, InterruptedException {

        final String path = request.getServletPath().replace(properties.getRootPath(), "");
        final String method = request.getMethod();

        final Api api = apiService.findOne(path, method);

        if (api == null) {
            log.debug("Not found the API registration that match this request. Path:{} Method:{}", path, method);
        }
        
        final String dataKey = dataKeyExtractor.extract(api, request, requestEntity);

        final ApiEvidence evidence = apiEvidenceFactory.create(request, dataKey);

        try {

            evidence.start();
            evidence.request(request, requestEntity);

            boolean enabledProxy = Optional.ofNullable(api)
                    .map(Api::getProxy)
                    .map(ApiProxy::getEnabled)
                    .orElse(properties.getProxy().isDefaultEnabled());

            final ResponseEntity<Object> responseEntity;
            if (enabledProxy) {
                responseEntity = proxyHandler.perform(request, requestEntity, path, method, dataKey, api, evidence);

            } else {
                responseEntity = mockResponseHandler.perform(path, method, dataKey, requestEntity, request, response, evidence);

            }

            evidence.response(responseEntity);

            return responseEntity;

        } finally {
            evidence.end();
        }

    }


}
