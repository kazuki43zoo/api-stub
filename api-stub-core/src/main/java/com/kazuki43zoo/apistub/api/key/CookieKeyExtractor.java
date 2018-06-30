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
package com.kazuki43zoo.apistub.api.key;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Order(7)
public class CookieKeyExtractor implements KeyExtractor {
  @Override
  public List<Object> extract(HttpServletRequest request, byte[] requestBody, String... expressions) {
    if (request.getCookies() == null || expressions.length == 0) {
      return Collections.emptyList();
    }

    Map<String, String> cookieMap = Stream.of(request.getCookies())
        .collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
    return Stream.of(expressions).map(cookieMap::get)
        .collect(Collectors.toList());
  }
}
