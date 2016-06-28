package com.kazuki43zoo.component;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Component
public class DownloadSupport {

    private static final String CONTENT_DISPOSITION_FORMAT = "attachment; filename=\"%s\"; filename*=UTF-8''%s";

    public void addContentDisposition(HttpHeaders headers, String fileName) throws UnsupportedEncodingException {
        String headerValue = String.format(CONTENT_DISPOSITION_FORMAT,
                fileName, UriUtils.encode(fileName, StandardCharsets.UTF_8.name()));
        headers.add(HttpHeaders.CONTENT_DISPOSITION, headerValue);
    }

}
