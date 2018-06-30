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

import javax.xml.bind.JAXB;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class XPathKeyExtractorTests {

  private static final XPathKeyExtractor extractor = new XPathKeyExtractor();

  @Test
  public void testBodyIsNull() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    List<Object> keys = extractor.extract(request, null, "key");
    assertThat(keys).isEmpty();
  }

  @Test
  public void testBodyIsEmpty() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    List<Object> keys = extractor.extract(request, new byte[0], "//key/text()");
    assertThat(keys).isEmpty();
  }

  @Test
  public void testExpressionIsMatch() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    OnePropertyBean xmlSource = new OnePropertyBean();
    xmlSource.setKey("value");

    StringWriter xmlWriter = new StringWriter();
    JAXB.marshal(xmlSource, xmlWriter);

    List<Object> keys = extractor.extract(request, xmlWriter.toString().getBytes(StandardCharsets.UTF_8), "//key/text()");
    assertThat(keys).hasSize(1);
    assertThat(keys).containsSequence("value");
  }

  @Test
  public void testExpressionIsMatchByMultiple() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    MultiplePropertyBean xmlSource = new MultiplePropertyBean();
    xmlSource.setKey1("value1");
    xmlSource.setKey2("value2");
    xmlSource.setKey3("value3");

    StringWriter xmlWriter = new StringWriter();
    JAXB.marshal(xmlSource, xmlWriter);

    List<Object> keys = extractor.extract(request, xmlWriter.toString().getBytes(StandardCharsets.UTF_8), "//key1/text()", "//key2/text()",
        "//key4/text()");
    assertThat(keys).hasSize(3);
    assertThat(keys).containsSequence("value1", "value2", "");
  }

  @Test
  public void testExpressionIsNoMatch() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    OnePropertyBean xmlSource = new OnePropertyBean();
    xmlSource.setKey("value");

    StringWriter xmlWriter = new StringWriter();
    JAXB.marshal(xmlSource, xmlWriter);

    List<Object> keys = extractor.extract(request, xmlWriter.toString().getBytes(StandardCharsets.UTF_8), "//key2/text()");
    assertThat(keys).hasSize(1);
    assertThat(keys).containsSequence("");
  }

  @Test
  public void testExpressionIsEmpty() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    OnePropertyBean xmlSource = new OnePropertyBean();
    xmlSource.setKey("value");

    StringWriter xmlWriter = new StringWriter();
    JAXB.marshal(xmlSource, xmlWriter);

    List<Object> keys = extractor.extract(request, xmlWriter.toString().getBytes(StandardCharsets.UTF_8));
    assertThat(keys).isEmpty();
  }

  @Test(expected = IllegalStateException.class)
  public void testBodyIsWrong() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    extractor.extract(request, "<a></b>".getBytes(StandardCharsets.UTF_8));
  }

  @Test(expected = IllegalStateException.class)
  public void testExpressionIsWrong() {
    MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
        .buildRequest(new MockServletContext());

    extractor.extract(request, "<a></a>".getBytes(StandardCharsets.UTF_8), "//a/foo()");
  }

  static class OnePropertyBean {
    private String key;

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }
  }

  static class MultiplePropertyBean {
    private String key1;
    private String key2;
    private String key3;

    public String getKey1() {
      return key1;
    }

    public void setKey1(String key1) {
      this.key1 = key1;
    }

    public String getKey2() {
      return key2;
    }

    public void setKey2(String key2) {
      this.key2 = key2;
    }

    public String getKey3() {
      return key3;
    }

    public void setKey3(String key3) {
      this.key3 = key3;
    }
  }

}
