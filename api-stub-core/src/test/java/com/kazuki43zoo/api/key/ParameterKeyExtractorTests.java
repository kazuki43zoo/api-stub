package com.kazuki43zoo.api.key;

import java.util.List;

import javax.servlet.http.Cookie;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class ParameterKeyExtractorTests {

	private static final ParameterKeyExtractor extractor = new ParameterKeyExtractor();

	@Test
	public void testParameterIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key");
		Assertions.assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsMatch() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key", "value")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key");
		Assertions.assertThat(keys).hasSize(1);
		Assertions.assertThat(keys).containsSequence("value");
	}

	@Test
	public void testExpressionIsMatchByMultiple() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key1", "value1")
				.param("key2", "value2").param("key3", "value3").buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key1", "key2", "key4");
		Assertions.assertThat(keys).hasSize(2);
		Assertions.assertThat(keys).containsSequence("value1", "value2");
	}

	@Test
	public void testExpressionIsNoMatch() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key", "value")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key2");
		Assertions.assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key", "value")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null);
		Assertions.assertThat(keys).isEmpty();
	}

}
