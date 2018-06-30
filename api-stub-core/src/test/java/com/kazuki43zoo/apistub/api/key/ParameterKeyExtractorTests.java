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

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterKeyExtractorTests {

  private static final ParameterKeyExtractor extractor = new ParameterKeyExtractor();

  @Test
  public void testParameterIsEmpty() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    List<Object> keys = extractor.extract(request, null, "key");
    assertThat(keys).hasSize(1);
    assertThat(keys).containsSequence((String) null);
  }

  @Test
  public void testExpressionIsMatch() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key", "value")
        .buildRequest(new MockServletContext());

    List<Object> keys = extractor.extract(request, null, "key");
    assertThat(keys).hasSize(1);
    assertThat(keys).containsSequence("value");
  }

  @Test
  public void testExpressionIsMatchByMultiple() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key1", "value1")
        .param("key2", "value2").param("key3", "value3").buildRequest(new MockServletContext());

    List<Object> keys = extractor.extract(request, null, "key1", "key2", "key4");
    assertThat(keys).hasSize(3);
    assertThat(keys).containsSequence("value1", "value2", null);
  }

  @Test
  public void testExpressionIsNoMatch() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key", "value")
        .buildRequest(new MockServletContext());

    List<Object> keys = extractor.extract(request, null, "key2");
    assertThat(keys).hasSize(1);
    assertThat(keys).containsSequence((String) null);
  }

  @Test
  public void testExpressionIsEmpty() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key", "value")
        .buildRequest(new MockServletContext());

    List<Object> keys = extractor.extract(request, null);
    assertThat(keys).isEmpty();
  }

}
