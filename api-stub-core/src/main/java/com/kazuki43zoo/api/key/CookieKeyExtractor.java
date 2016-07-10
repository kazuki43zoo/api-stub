package com.kazuki43zoo.api.key;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(5)
public class CookieKeyExtractor implements KeyExtractor {
    @Override
    public List<String> extract(HttpServletRequest request, String requestBody, String... expressions) {
        if (request.getCookies() == null) {
            return null;
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
