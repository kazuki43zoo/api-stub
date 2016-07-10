package com.kazuki43zoo.api.key;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class JsonPathKeyExtractor implements KeyExtractor {
    @Override
    public List<String> extract(HttpServletRequest request, String requestBody, String... expressions) {
        List<String> values = new ArrayList<>();
        ReadContext context = JsonPath.parse(requestBody);
        for (String expression : expressions) {
            try {
                String id = context.read(expression);
                if (StringUtils.hasLength(id)) {
                    values.add(id);
                }
            } catch (Exception e) {
                // skip
            }
        }
        return values;
    }
}
