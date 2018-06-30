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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FixedLengthKeyExtractorTests {

  private static final FixedLengthKeyExtractor extractor = new FixedLengthKeyExtractor();

  @Test
  public void testBodyIsNull() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    List<Object> keys = extractor.extract(request, null, "0,10,string");
    assertThat(keys).isEmpty();
  }

  @Test
  public void testBodyIsEmpty() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    List<Object> keys = extractor.extract(request, new byte[0], "0,10,string");
    assertThat(keys).isEmpty();
  }

  @Test
  public void testExpressionIsMatch() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    String body = "0123456789a";
    List<Object> keys = extractor.extract(request, body.getBytes(StandardCharsets.UTF_8), "0,10,string");
    assertThat(keys)
        .hasSize(1)
        .containsSequence("0123456789");
  }

  @Test
  public void testExpressionIsMatchByMultiple() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    String body = "0123456789abcdefghijk";

    List<Object> keys = extractor.extract(request, body.getBytes(StandardCharsets.UTF_8), "0,10,string", " 10 , 10 , STRING ");
    assertThat(keys)
        .hasSize(2)
        .containsSequence("0123456789", "abcdefghij");
  }

  @Test
  public void testBinaryTypes() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    byte[] body = ByteBuffer.allocate(2 + 4 + 8 + 4 + 8)
        .putShort(Short.MAX_VALUE)
        .putInt(Integer.MAX_VALUE)
        .putLong(Long.MAX_VALUE)
        .putFloat(Float.MAX_VALUE)
        .putDouble(Double.MAX_VALUE)
        .array();
    List<Object> keys = extractor.extract(request, body, "0,2,short", "2,4,int", "6,8,long", "14,4,float", "18,8,double");
    assertThat(keys)
        .hasSize(5)
        .containsSequence(Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE);
  }

  @Test
  public void testExpressionIsNoMatch() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    String body = "0123456789abcdefghij";

    List<Object> keys = extractor.extract(request, body.getBytes(StandardCharsets.UTF_8), "20,10,string", "30,10,string");
    assertThat(keys).isEmpty();
  }

  @Test
  public void testExpressionIsEmpty() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    String body = "0123456789abcdefghij";

    List<Object> keys = extractor.extract(request, body.getBytes(StandardCharsets.UTF_8));
    assertThat(keys).isEmpty();
  }

  @Test
  public void testExpressionIsIgnore() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    String body = "0123456789abcdefghij";

    List<Object> keys = extractor.extract(request, body.getBytes(StandardCharsets.UTF_8), "0", "0,10");
    assertThat(keys).isEmpty();
  }

  @Test
  public void testRequestedCharset() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .contentType(MediaType.APPLICATION_OCTET_STREAM.toString() + ";charset=Windows-31J")
        .buildRequest(new MockServletContext());

    String body = "あいうえおa";
    List<Object> keys = extractor.extract(request, body.getBytes(Charset.forName("Windows-31J")), "0,10,string");
    assertThat(keys)
        .hasSize(1)
        .containsSequence("あいうえお");
  }

  @Test
  public void testDefinedCharset() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    String body = "あいうえおa";
    List<Object> keys = extractor.extract(request, body.getBytes(Charset.forName("Windows-31J")), "0,10,string,Windows-31J");
    assertThat(keys)
        .hasSize(1)
        .containsSequence("あいうえお");
  }

  @Test
  public void testDefaultCharset() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    String body = "あいうab";
    List<Object> keys = extractor.extract(request, body.getBytes(StandardCharsets.UTF_8), "0,10,string");
    assertThat(keys)
        .hasSize(1)
        .containsSequence("あいうa");
  }

  @Test(expected = NumberFormatException.class)
  public void testExpressionIsWrongOffsetIsNotNumber() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    extractor.extract(request, "123".getBytes(StandardCharsets.UTF_8), "a,10,string");
  }

  @Test(expected = NumberFormatException.class)
  public void testExpressionIsWrongLengthIsNotNumber() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    extractor.extract(request, "123".getBytes(StandardCharsets.UTF_8), "0,a,string");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExpressionIsWrongTypeIsNotSupport() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    extractor.extract(request, "123".getBytes(StandardCharsets.UTF_8), "0,3,varchar");
  }

  @Test(expected = UnsupportedCharsetException.class)
  public void testExpressionIsWrongCharsetIsNotExist() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    extractor.extract(request, "123".getBytes(StandardCharsets.UTF_8), "0,3,string,aaa");
  }

}
