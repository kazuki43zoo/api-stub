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

import com.kazuki43zoo.api.handler.ApiStubRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiStubFilter extends GenericFilterBean {

  private final ApiStubRequestHandler requestHandler;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
      throw new ServletException("ApiStubFilter just supports HTTP requests");
    }
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    ServletServerHttpRequest serverRequest = new ServletServerHttpRequest(httpRequest);
    RequestEntity<byte[]> requestEntity;
    if (serverRequest.getMethod() == HttpMethod.GET || serverRequest.getMethod() == HttpMethod.HEAD) {
      requestEntity = new RequestEntity<>(serverRequest.getHeaders(), serverRequest.getMethod(), serverRequest.getURI());
    } else {
      requestEntity = new RequestEntity<>(StreamUtils.copyToByteArray(serverRequest.getBody()), serverRequest.getHeaders(), serverRequest.getMethod(), serverRequest.getURI());
    }

    ResponseEntity<Resource> responseEntity = requestHandler.handleApiRequest(httpRequest, httpResponse, requestEntity);

    ServletServerHttpResponse serverResponse = new ServletServerHttpResponse(httpResponse);
    serverResponse.setStatusCode(responseEntity.getStatusCode());
    serverResponse.getHeaders().addAll(responseEntity.getHeaders());
    if (responseEntity.getBody() != null) {
      StreamUtils.copy(responseEntity.getBody().getInputStream(), serverResponse.getBody());
    }
  }

}
