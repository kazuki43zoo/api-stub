package com.kazuki43zoo.api.key;

import java.util.List;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

public class HeaderKeyExtractorTests {

	private static final HeaderKeyExtractor extractor = new HeaderKeyExtractor();

	@Test
	public void testParameterIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsMatch() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").header("key", "value")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key");
		assertThat(keys).hasSize(1);
		assertThat(keys).containsSequence("value");
	}

	@Test
	public void testExpressionIsMatchByMultiple() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").header("key1", "value1")
				.header("key2", "value2").header("key3", "value3").buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key1", "key2", "key4");
		assertThat(keys).hasSize(2);
		assertThat(keys).containsSequence("value1", "value2");
	}

	@Test
	public void testExpressionIsNoMatch() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").header("key", "value")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key2");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test").header("key", "value")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null);
		assertThat(keys).isEmpty();
	}

}
