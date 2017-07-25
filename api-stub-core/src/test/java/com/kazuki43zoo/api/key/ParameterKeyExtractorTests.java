package com.kazuki43zoo.api.key;

import java.util.List;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterKeyExtractorTests {

	private static final ParameterKeyExtractor extractor = new ParameterKeyExtractor();

	@Test
	public void testParameterIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsMatch() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key", "value")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key");
		assertThat(keys).hasSize(1);
		assertThat(keys).containsSequence("value");
	}

	@Test
	public void testExpressionIsMatchByMultiple() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key1", "value1")
				.param("key2", "value2").param("key3", "value3").buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key1", "key2", "key4");
		assertThat(keys).hasSize(2);
		assertThat(keys).containsSequence("value1", "value2");
	}

	@Test
	public void testExpressionIsNoMatch() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key", "value")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key2");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").param("key", "value")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null);
		assertThat(keys).isEmpty();
	}

}
