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
package com.kazuki43zoo.api.key;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@Order(3)
public class FixedLengthKeyExtractor implements KeyExtractor {

  private static final Map<String, BiFunction<byte[], Charset, Object>> BYTES_TO_OBJECT_FUNCTIONS;

  static {
    Map<String, BiFunction<byte[], Charset, Object>> bytesToObjectFunctions = new HashMap<>();
    bytesToObjectFunctions.put("string", String::new);
    bytesToObjectFunctions.put("short", (bytes, charset) -> ByteBuffer.wrap(bytes).getShort());
    bytesToObjectFunctions.put("int", (bytes, charset) -> ByteBuffer.wrap(bytes).getInt());
    bytesToObjectFunctions.put("long", (bytes, charset) -> ByteBuffer.wrap(bytes).getLong());
    bytesToObjectFunctions.put("char", (bytes, charset) -> ByteBuffer.wrap(bytes).getChar());
    bytesToObjectFunctions.put("float", (bytes, charset) -> ByteBuffer.wrap(bytes).getFloat());
    bytesToObjectFunctions.put("double", (bytes, charset) -> ByteBuffer.wrap(bytes).getDouble());
    BYTES_TO_OBJECT_FUNCTIONS = Collections.unmodifiableMap(bytesToObjectFunctions);
  }

  @Override
  public List<Object> extract(HttpServletRequest request, byte[] requestBody, String... expressions) {
    if (requestBody == null || requestBody.length == 0) {
      return Collections.emptyList();
    }

    Charset defaultCharset = Optional.ofNullable(request.getContentType())
        .map(MediaType::parseMediaType)
        .map(MediaType::getCharset)
        .orElse(StandardCharsets.UTF_8);

    return Stream.of(expressions).map(expression -> {
      String[] extractionDefine = StringUtils.splitByWholeSeparatorPreserveAllTokens(expression, ",");
      if (extractionDefine.length <= 2) {
        return null;
      }
      int offset = Integer.parseInt(extractionDefine[0].trim());
      int length = Integer.parseInt(extractionDefine[1].trim());
      String type = extractionDefine[2].trim().toLowerCase();
      final Charset charset;
      if (extractionDefine.length >= 4) {
        charset = Charset.forName(extractionDefine[3].trim());
      } else {
        charset = defaultCharset;
      }
      if (!(requestBody.length >= offset && requestBody.length >= offset + length)) {
        return null;
      }
      return Optional.ofNullable(BYTES_TO_OBJECT_FUNCTIONS.get(type))
          .orElseThrow(() -> new IllegalArgumentException("A bad expression is detected. The specified type does not support. expression: '"
              + expression + "', specified type: '" + type + "', allowing types: " + BYTES_TO_OBJECT_FUNCTIONS.keySet()))
          .apply(Arrays.copyOfRange(requestBody, offset, offset + length), charset);
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }

}
