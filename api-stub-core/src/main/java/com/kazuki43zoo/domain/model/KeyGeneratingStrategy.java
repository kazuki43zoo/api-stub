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
package com.kazuki43zoo.domain.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum KeyGeneratingStrategy {

  ALL {
    @Override

    public String generate(List<Object> keys) {
      if (keys == null || keys.isEmpty()) {
        return null;
      }
      return join(keys);
    }
  };

  public static final String KEY_DELIMITER = "/";

  public static List<String> split(String keys) {
    return Arrays.asList(StringUtils.splitByWholeSeparatorPreserveAllTokens(keys, KEY_DELIMITER));
  }

  public static <T> String join(List<T> keys) {
    return keys.stream()
        .map(e -> e == null ? "" : e.toString())
        .collect(Collectors.joining(KEY_DELIMITER));
  }

  public abstract String generate(List<Object> values);

}
