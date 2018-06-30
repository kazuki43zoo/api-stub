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
package com.kazuki43zoo.api.key;

import com.kazuki43zoo.api.PathVariableSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
@Order(4)
public class PathVariableKeyExtractor implements KeyExtractor {

  private final PathVariableSupport pathVariableSupport;

  @Override
  public List<Object> extract(HttpServletRequest request, byte[] requestBody, String... expressions) {
    Map<String, String> pathVariable = pathVariableSupport.getPathVariables(request);
    return Stream.of(expressions).map(pathVariable::get)
        .collect(Collectors.toList());
  }

}
