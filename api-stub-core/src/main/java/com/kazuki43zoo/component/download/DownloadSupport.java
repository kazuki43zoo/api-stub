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
package com.kazuki43zoo.component.download;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DownloadSupport {

    private static final Pattern PATTERN_ASCII_ONLY = Pattern.compile("\\p{ASCII}*");
    private static final Pattern PATTERN_FOR_FILENAME = Pattern.compile(".*filename=\"(.*)\".*");
    private static final Pattern PATTERN_FOR_ENC_FILENAME = Pattern.compile(".*filename\\*=(.*)'.*'([^;]*).*");

    private static final String CONTENT_DISPOSITION_FORMAT_ASCII_ONLY = "attachment; filename=\"%s\";";
    private static final String CONTENT_DISPOSITION_FORMAT_NOT_ASCII = "attachment; filename*=UTF-8''%s";

    public void addContentDisposition(HttpHeaders headers, String fileName) throws UnsupportedEncodingException {
        String headerValue;
        if (PATTERN_ASCII_ONLY.matcher(fileName).matches()) {
            headerValue = String.format(CONTENT_DISPOSITION_FORMAT_ASCII_ONLY, fileName);
        } else {
            headerValue = String.format(CONTENT_DISPOSITION_FORMAT_NOT_ASCII, UriUtils.encode(fileName, StandardCharsets.UTF_8.name()));
        }
        headers.add(HttpHeaders.CONTENT_DISPOSITION, headerValue);
    }

    public String extractDownloadFileName(HttpHeaders httpHeaders) throws UnsupportedEncodingException {
        final String contentDisposition = httpHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (!StringUtils.hasLength(contentDisposition)) {
            return null;
        }
        {
            final Matcher matcher = PATTERN_FOR_ENC_FILENAME.matcher(contentDisposition);
            if (matcher.find() && matcher.groupCount() > 1) {
                return UriUtils.decode(matcher.group(2), matcher.group(1));
            }
        }
        {
            Matcher matcher = PATTERN_FOR_FILENAME.matcher(contentDisposition);
            if (matcher.find() && matcher.groupCount() > 0) {
                return matcher.group(1);
            }
        }
        return null;
    }

}
