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

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@Order(1)
public class JsonPathKeyExtractor implements KeyExtractor {
    @Override
    public List<String> extract(HttpServletRequest request, String requestBody, String... expressions) {
        if (StringUtils.isEmpty(requestBody)) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        ReadContext context = JsonPath.parse(requestBody);
        for (String expression : expressions) {
            try {
                String id = context.read(expression);
                if (StringUtils.hasLength(id)) {
                    values.add(id);
                }
            } catch (Exception e) {
                // ignore
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage(), e);
                }
            }
        }
        return values;
    }
}
