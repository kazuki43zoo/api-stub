package com.kazuki43zoo.api.key;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class JsonPathKeyExtractorTests {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final JsonPathKeyExtractor extractor = new JsonPathKeyExtractor();

	@Test
	public void testBodyIsNull() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "$.key");
		Assertions.assertThat(keys).isEmpty();
	}

	@Test
	public void testBodyIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, "", "$.key");
		Assertions.assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsMatch() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		Map<String, Object> jsonSource = new HashMap<>();
		jsonSource.put("key", "value");

		List<String> keys = extractor.extract(request, objectMapper.writeValueAsString(jsonSource), "$.key");
		Assertions.assertThat(keys).hasSize(1);
		Assertions.assertThat(keys).containsSequence("value");
	}

	@Test
	public void testExpressionIsMatchByMultiple() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		Map<String, Object> jsonSource = new HashMap<>();
		jsonSource.put("key1", "value1");
		jsonSource.put("key2", "value2");
		jsonSource.put("key3", "value3");

		List<String> keys = extractor.extract(request, objectMapper.writeValueAsString(jsonSource), "$.key1", "$.key2",
				"$.key4");
		Assertions.assertThat(keys).hasSize(2);
		Assertions.assertThat(keys).containsSequence("value1", "value2");
	}

	@Test
	public void testExpressionIsNoMatch() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		Map<String, Object> jsonSource = new HashMap<>();
		jsonSource.put("key", "value");

		List<String> keys = extractor.extract(request, objectMapper.writeValueAsString(jsonSource), "$.key2");
		Assertions.assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsEmpty() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		Map<String, Object> jsonSource = new HashMap<>();
		jsonSource.put("key", "value");

		List<String> keys = extractor.extract(request, objectMapper.writeValueAsString(jsonSource));
		Assertions.assertThat(keys).isEmpty();
	}

}
