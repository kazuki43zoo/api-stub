package com.kazuki43zoo.api.key;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(3)
public class ParameterKeyExtractor implements KeyExtractor {
    @Override
    public List<String> extract(HttpServletRequest request, String requestBody, String... expressions) {
        List<String> values = new ArrayList<>();
        for (String expression : expressions) {
            String id = request.getParameter(expression);
            if (StringUtils.hasLength(id)) {
                values.add(id);
            }
        }
        return values;
    }
}
