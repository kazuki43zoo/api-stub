package com.kazuki43zoo.api.key;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.xpath.XPath;

public class XPathKeyExtractorTests {

	private static final XPathKeyExtractor extractor = new XPathKeyExtractor();

	@Test
	public void testBodyIsNull() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key");
		Assertions.assertThat(keys).isEmpty();
	}

	@Test
	public void testBodyIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, "", "//key/text()");
		Assertions.assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsMatch() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		OnePropertyBean xmlSource = new OnePropertyBean();
		xmlSource.setKey("value");

		StringWriter xmlWriter = new StringWriter();
		JAXB.marshal(xmlSource, xmlWriter);

		System.out.println(xmlWriter.toString());

		List<String> keys = extractor.extract(request, xmlWriter.toString(), "//key/text()");
		Assertions.assertThat(keys).hasSize(1);
		Assertions.assertThat(keys).containsSequence("value");
	}

	@Test
	public void testExpressionIsMatchByMultiple() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		MultiplePropertyBean xmlSource = new MultiplePropertyBean();
		xmlSource.setKey1("value1");
		xmlSource.setKey2("value2");
		xmlSource.setKey3("value3");

		StringWriter xmlWriter = new StringWriter();
		JAXB.marshal(xmlSource, xmlWriter);

		List<String> keys = extractor.extract(request, xmlWriter.toString(), "//key1/text()", "//key2/text()",
				"//key4/text()");
		Assertions.assertThat(keys).hasSize(2);
		Assertions.assertThat(keys).containsSequence("value1", "value2");
	}

	@Test
	public void testExpressionIsNoMatch() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		OnePropertyBean xmlSource = new OnePropertyBean();
		xmlSource.setKey("value");

		StringWriter xmlWriter = new StringWriter();
		JAXB.marshal(xmlSource, xmlWriter);

		List<String> keys = extractor.extract(request, xmlWriter.toString(), "//key2/text()");
		Assertions.assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsEmpty() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		OnePropertyBean xmlSource = new OnePropertyBean();
		xmlSource.setKey("value");

		StringWriter xmlWriter = new StringWriter();
		JAXB.marshal(xmlSource, xmlWriter);

		List<String> keys = extractor.extract(request, xmlWriter.toString());
		Assertions.assertThat(keys).isEmpty();
	}

	@Test(expected = IllegalStateException.class)
	public void testBodyIsWrong() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, "<a></b>");
		Assertions.assertThat(keys).isEmpty();
	}

	@Test(expected = IllegalStateException.class)
	public void testExpressionIsWrong() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, "<a></a>", "//a/foo()");
		Assertions.assertThat(keys).isEmpty();
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
