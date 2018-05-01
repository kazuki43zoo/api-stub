/*
 *    Copyright 2016-2017 the original author or authors.
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
package com.kazuki43zoo.api.key;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPathKeyExtractorTests {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final JsonPathKeyExtractor extractor = new JsonPathKeyExtractor();

	@Test
	public void testBodyIsNull() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<Object> keys = extractor.extract(request, null, "$.key");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testBodyIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<Object> keys = extractor.extract(request, new byte[0], "$.key");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsMatch() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		Map<String, Object> jsonSource = new HashMap<>();
		jsonSource.put("key", "value");

		List<Object> keys = extractor.extract(request, objectMapper.writeValueAsBytes(jsonSource), "$.key");
		assertThat(keys).hasSize(1);
		assertThat(keys).containsSequence("value");
	}

	@Test
	public void testExpressionIsMatchByMultiple() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		Map<String, Object> jsonSource = new HashMap<>();
		jsonSource.put("key1", "value1");
		jsonSource.put("key2", "value2");
		jsonSource.put("key3", "value3");

		List<Object> keys = extractor.extract(request, objectMapper.writeValueAsBytes(jsonSource), "$.key1", "$.key2",
				"$.key4");
		assertThat(keys).hasSize(2);
		assertThat(keys).containsSequence("value1", "value2");
	}

	@Test
	public void testExpressionIsNoMatch() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		Map<String, Object> jsonSource = new HashMap<>();
		jsonSource.put("key", "value");

		List<Object> keys = extractor.extract(request, objectMapper.writeValueAsBytes(jsonSource), "$.key2");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsEmpty() throws JsonProcessingException {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		Map<String, Object> jsonSource = new HashMap<>();
		jsonSource.put("key", "value");

		List<Object> keys = extractor.extract(request, objectMapper.writeValueAsBytes(jsonSource));
		assertThat(keys).isEmpty();
	}

}
