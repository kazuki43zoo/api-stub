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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PathVariableSupport {

  private static final String ATTRIBUTE_KEY = PathVariableSupport.class.getName() + ".pathVariables";

  private final PathMatcher pathMatcher;

  public void storePathVariables(String pathPattern, String path, HttpServletRequest request) {
    Map<String, String> pathVariables = pathMatcher.extractUriTemplateVariables(pathPattern, path);
    request.setAttribute(ATTRIBUTE_KEY, pathVariables);
  }

  public Map<String, String> getPathVariables(HttpServletRequest request) {
    @SuppressWarnings("unchecked")
    Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(ATTRIBUTE_KEY);
    return Optional.ofNullable(pathVariables).orElse(Collections.emptyMap());
  }

}
