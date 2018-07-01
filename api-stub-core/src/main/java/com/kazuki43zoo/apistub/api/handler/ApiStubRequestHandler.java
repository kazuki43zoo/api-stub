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
package com.kazuki43zoo.apistub.api.handler;

import com.kazuki43zoo.apistub.api.evidence.ApiEvidence;
import com.kazuki43zoo.apistub.api.evidence.ApiEvidenceFactory;
import com.kazuki43zoo.apistub.api.key.DataKeySupport;
import com.kazuki43zoo.apistub.api.PathVariableSupport;
import com.kazuki43zoo.apistub.api.ApiStubProperties;
import com.kazuki43zoo.apistub.domain.model.Api;
import com.kazuki43zoo.apistub.domain.model.ApiProxy;
import com.kazuki43zoo.apistub.domain.service.ApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.io.Resource;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Component
public class ApiStubRequestHandler {

  private final ApiService apiService;
  private final ApiStubProperties properties;
  private final ApiEvidenceFactory apiEvidenceFactory;
  private final MockResponseHandler mockResponseHandler;
  private final ProxyHandler proxyHandler;
  private final DataKeySupport dataKeySupport;
  private final PathVariableSupport pathVariableSupport;
  private Pattern rootPathPattern;

  @PostConstruct
  public void setupRootPathPattern() {
    this.rootPathPattern = Pattern.compile(properties.getRootPath());
  }

  public ResponseEntity<Resource> handleApiRequest(HttpServletRequest request, HttpServletResponse response, RequestEntity<byte[]> requestEntity)
      throws IOException, ServletException {

    ApiEvidence evidence = null;
    try {
      final String correlationId = Optional.ofNullable(request.getHeader(properties.getCorrelationIdKey()))
          .orElse(UUID.randomUUID().toString());
      MDC.put(properties.getCorrelationIdKey(), correlationId);

      final String path = rootPathPattern.matcher(request.getServletPath()).replaceAll("");
      final String method = request.getMethod();

      final Api api = apiService.findOne(path, method);

      if (api == null) {
        log.debug("Not found the API registration that match this request. Path:{} Method:{}", path, method);
      } else {
        pathVariableSupport.storePathVariables(api.getPath(), path, request);
      }

      final String dataKey = dataKeySupport.extractDataKey(api, request, requestEntity);

      evidence = apiEvidenceFactory.create(request, method, path, dataKey, correlationId, api);

      evidence.start();
      evidence.request(request, requestEntity);

      boolean enabledProxy = Optional.ofNullable(api)
          .map(Api::getProxy)
          .map(ApiProxy::getEnabled)
          .orElse(properties.getProxy().isDefaultEnabled());

      final ResponseEntity<Resource> responseEntity;
      if (enabledProxy) {
        responseEntity = proxyHandler.perform(request, requestEntity, path, method, dataKey, api, evidence);
      } else {
        responseEntity = mockResponseHandler.perform(path, method, dataKey, requestEntity, request, response, api, evidence);
      }

      evidence.response(responseEntity);

      return responseEntity;

    } finally {
      Optional.ofNullable(evidence).ifPresent(ApiEvidence::end);
      MDC.clear();
    }

  }

}
