package com.kazuki43zoo.api.key;

import java.io.StringWriter;
import java.util.List;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.xml.bind.JAXB;

import static org.assertj.core.api.Assertions.assertThat;

public class XPathKeyExtractorTests {

	private static final XPathKeyExtractor extractor = new XPathKeyExtractor();

	@Test
	public void testBodyIsNull() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testBodyIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, "", "//key/text()");
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

		List<String> keys = extractor.extract(request, xmlWriter.toString(), "//key/text()");
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

		List<String> keys = extractor.extract(request, xmlWriter.toString(), "//key1/text()", "//key2/text()",
				"//key4/text()");
		assertThat(keys).hasSize(2);
		assertThat(keys).containsSequence("value1", "value2");
	}

	@Test
	public void testExpressionIsNoMatch() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		OnePropertyBean xmlSource = new OnePropertyBean();
		xmlSource.setKey("value");

		StringWriter xmlWriter = new StringWriter();
		JAXB.marshal(xmlSource, xmlWriter);

		List<String> keys = extractor.extract(request, xmlWriter.toString(), "//key2/text()");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		OnePropertyBean xmlSource = new OnePropertyBean();
		xmlSource.setKey("value");

		StringWriter xmlWriter = new StringWriter();
		JAXB.marshal(xmlSource, xmlWriter);

		List<String> keys = extractor.extract(request, xmlWriter.toString());
		assertThat(keys).isEmpty();
	}

	@Test(expected = IllegalStateException.class)
	public void testBodyIsWrong() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		extractor.extract(request, "<a></b>");
	}

	@Test(expected = IllegalStateException.class)
	public void testExpressionIsWrong() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		extractor.extract(request, "<a></a>", "//a/foo()");
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
