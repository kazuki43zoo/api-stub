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
import com.kazuki43zoo.apistub.api.DownloadSupport;
import com.kazuki43zoo.apistub.api.config.ApiStubProperties;
import com.kazuki43zoo.apistub.domain.model.Api;
import com.kazuki43zoo.apistub.domain.model.ApiProxy;
import com.kazuki43zoo.apistub.domain.model.ApiResponse;
import com.kazuki43zoo.apistub.domain.service.ApiResponseService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.StringJoiner;

@Component
public class ProxyHandler {

  private static final String HEADER_SEPARATOR = "\r\n";
  private static final String HEADER_KEY_VALUE_SEPARATOR = ":";

  private final RestOperations restOperations;
  private final ApiResponseService apiResponseService;
  private final DownloadSupport downloadSupport;
  private final ApiStubProperties properties;

  public ProxyHandler(RestOperations restOperations,
                      ApiResponseService apiResponseService,
                      DownloadSupport downloadSupport,
                      ApiStubProperties properties) {
    this.restOperations = restOperations;
    this.apiResponseService = apiResponseService;
    this.downloadSupport = downloadSupport;
    this.properties = properties;
  }

  ResponseEntity<Resource> perform(HttpServletRequest request, RequestEntity<byte[]> requestEntity, String path, String method, String dataKey, Api api, ApiEvidence evidence) {

    final String baseUrl = Optional.ofNullable(api)
        .map(Api::getProxy)
        .map(ApiProxy::getUrl)
        .filter(StringUtils::hasLength)
        .orElse(properties.getProxy().getDefaultUrl());

    final String url = baseUrl + path +
        (StringUtils.hasLength(request.getQueryString()) ? "?" + request.getQueryString() : "");

    final RequestEntity.BodyBuilder requestBodyBuilder =
        RequestEntity.method(HttpMethod.valueOf(method.toUpperCase()), URI.create(url));

    Collections.list(request.getHeaderNames())
        .forEach(name -> requestBodyBuilder.header(name, Collections.list(request.getHeaders(name)).toArray(new String[0])));

    evidence.info("Proxy to {}", () -> evidence.toArray(url));

    final ResponseEntity<byte[]> proxyResponseEntity =
        restOperations.exchange(requestBodyBuilder.body(requestEntity.getBody()), byte[].class);

    final HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.putAll(proxyResponseEntity.getHeaders());

    final Resource responseBody = new InputStreamResource(new ByteArrayInputStream(proxyResponseEntity.getBody()));
    responseHeaders.remove(HttpHeaders.TRANSFER_ENCODING);

    boolean enabledCapturing = Optional.ofNullable(api)
        .map(Api::getProxy)
        .map(ApiProxy::getCapturing)
        .orElse(properties.getProxy().isDefaultCapturing());

    if (enabledCapturing) {
      doCapture(path, method, dataKey, proxyResponseEntity, responseHeaders, evidence);
    }

    return ResponseEntity.status(proxyResponseEntity.getStatusCodeValue())
        .headers(responseHeaders)
        .body(responseBody);
  }

  private void doCapture(String path, String method, String dataKey, ResponseEntity<byte[]> proxyResponseEntity, HttpHeaders responseHeaders, ApiEvidence evidence) {
    final ApiResponse apiResponse = new ApiResponse();
    apiResponse.setPath(path);
    apiResponse.setMethod(method);
    apiResponse.setDataKey(dataKey);
    apiResponse.setStatusCode(proxyResponseEntity.getStatusCodeValue());
    final StringJoiner headersJoiner = new StringJoiner(HEADER_SEPARATOR);
    responseHeaders.forEach((name, values) -> values.forEach(value -> headersJoiner.add(name + HEADER_KEY_VALUE_SEPARATOR + " " + value)));
    apiResponse.setHeader(headersJoiner.toString());
    apiResponse.setFileName(downloadSupport.extractDownloadFileName(responseHeaders));
    if (apiResponse.getFileName() != null) {
      apiResponse.setAttachmentFile(new ByteArrayInputStream(proxyResponseEntity.getBody()));
    } else {
      apiResponse.setBody(new ByteArrayInputStream(proxyResponseEntity.getBody()));
    }
    apiResponseService.createProxyResponse(apiResponse);

    evidence.info("Saved a proxy response into api_proxy_response. id = {}", () -> evidence.toArray(apiResponse.getId()));

  }

}
