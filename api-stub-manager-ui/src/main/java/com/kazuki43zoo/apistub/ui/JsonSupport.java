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
package com.kazuki43zoo.apistub.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazuki43zoo.apistub.domain.model.Api;
import com.kazuki43zoo.apistub.domain.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JsonSupport {

  private final ObjectMapper objectMapper;

  @SuppressWarnings("unchecked")
  public <T> List<T> toList(String json) {
    try {
      return (List<T>) objectMapper.readValue(json, List.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public String toJson(List<?> list) throws JsonProcessingException {
    return objectMapper.writeValueAsString(list);
  }

  public List<Api> toApiList(InputStream json) throws IOException {
    return Arrays.asList(objectMapper.readValue(json, Api[].class));
  }

  public List<ApiResponse> toApiResponseList(InputStream json) throws IOException {
    return Arrays.asList(objectMapper.readValue(json, ApiResponse[].class));
  }

}
