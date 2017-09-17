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

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CookieKeyExtractorTests {

	private static final CookieKeyExtractor extractor = new CookieKeyExtractor();

	@Test
	public void testCookieIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsMatch() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.cookie(new Cookie("key", "value")).buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key");
		assertThat(keys).hasSize(1);
		assertThat(keys).containsSequence("value");
	}

	@Test
	public void testExpressionIsMatchByMultiple() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.cookie(new Cookie("key1", "value1")).cookie(new Cookie("key2", "value2")).cookie(new Cookie("key3", "value3"))
				.buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key1", "key2", "key4");
		assertThat(keys).hasSize(2);
		assertThat(keys).containsSequence("value1", "value2");
	}

	@Test
	public void testExpressionIsNoMatch() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.cookie(new Cookie("key", "value")).buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null, "key2");
		assertThat(keys).isEmpty();
	}

	@Test
	public void testExpressionIsEmpty() {
		MockHttpServletRequest request = MockMvcRequestBuilders.request(HttpMethod.GET, "/test")
				.cookie(new Cookie("key", "value")).buildRequest(new MockServletContext());

		List<String> keys = extractor.extract(request, null);
		assertThat(keys).isEmpty();
	}

}
