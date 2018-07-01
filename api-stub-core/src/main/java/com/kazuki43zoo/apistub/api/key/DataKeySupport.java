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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazuki43zoo.apistub.domain.model.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class DataKeySupport {

  private static final Logger log = LoggerFactory.getLogger(DataKeySupport.class);

  private final Map<String, KeyExtractor> keyExtractorMap;
  private final ObjectMapper jsonObjectMapper;


  public DataKeySupport(Map<String, KeyExtractor> keyExtractorMap, ObjectMapper jsonObjectMapper) {
    this.keyExtractorMap = keyExtractorMap;
    this.jsonObjectMapper = jsonObjectMapper;
  }

  public String extractDataKey(Api api, HttpServletRequest request, RequestEntity<byte[]> requestEntity) throws IOException {
    if (api == null) {
      return null;
    }
    KeyExtractor keyExtractor = keyExtractorMap.get(api.getKeyExtractor());
    if (keyExtractor == null || api.getExpressions() == null || api.getKeyGeneratingStrategy() == null) {
      return null;
    }
    String[] expressions = Stream.of(jsonObjectMapper.readValue(api.getExpressions(), String[].class))
        .filter(StringUtils::hasLength)
        .toArray(String[]::new);
    String key = null;
    try {
      List<Object> keys = keyExtractor.extract(request, requestEntity.getBody(), expressions);
      key = api.getKeyGeneratingStrategy().generate(keys);
    } catch (Exception e) {
      // ignore
      if (log.isWarnEnabled()) {
        log.warn(e.getMessage(), e);
      }
    }
    return key;
  }

  public boolean isPathVariableDataKey(Api api) {
    if (api == null) {
      return false;
    }
    KeyExtractor keyExtractor = keyExtractorMap.get(api.getKeyExtractor());
    if (keyExtractor == null) {
      return false;
    }
    return PathVariableKeyExtractor.class == keyExtractor.getClass();
  }

}
