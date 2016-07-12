package com.kazuki43zoo.api.key;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface KeyExtractor {
    List<String> extract(HttpServletRequest request, String requestBody, String... expressions) throws Exception;
}
