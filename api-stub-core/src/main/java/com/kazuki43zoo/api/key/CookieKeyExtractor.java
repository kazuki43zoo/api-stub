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
package com.kazuki43zoo.api.key;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Order(5)
public class CookieKeyExtractor implements KeyExtractor {
    @Override
    public List<String> extract(HttpServletRequest request, String requestBody, String... expressions) {
        if (request.getCookies() == null) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        for (String expression : expressions) {
            for (Cookie cookie : request.getCookies()) {
                if (!cookie.getName().equals(expression)) {
                    continue;
                }
                String id = cookie.getValue();
                if (StringUtils.hasLength(id)) {
                    values.add(id);
                }
            }
        }
        return values;
    }
}
