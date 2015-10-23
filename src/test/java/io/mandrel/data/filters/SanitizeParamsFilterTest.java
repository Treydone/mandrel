/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License")); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.data.filters;

import io.mandrel.data.Link;
import io.mandrel.data.filters.link.SanitizeParamsFilter;

import java.net.URI;
import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class SanitizeParamsFilterTest {

	private SanitizeParamsFilter filter;

	@Before
	public void beforeEashTest() {
		filter = new SanitizeParamsFilter();
	}

	@Test
	public void no_link() {
		Assertions.assertThat(filter.isValid(new Link())).isTrue();
	}

	@Test
	public void link_without_params() {
		Link link = new Link().uri(URI.create("http://localhost/1"));
		Assertions.assertThat(filter.isValid(link)).isTrue();
		Assertions.assertThat(link.uri()).isEqualTo(URI.create("http://localhost/1"));
	}

	@Test
	public void link_without_valid_params() {
		Link link = new Link().uri(URI.create("http://localhost/1?"));
		Assertions.assertThat(filter.isValid(link)).isTrue();
		Assertions.assertThat(link.uri()).isEqualTo(URI.create("http://localhost/1"));
	}

	@Test
	public void link_with_param_empty() {
		Link link = new Link().uri(URI.create("http://localhost/1?test"));
		Assertions.assertThat(filter.isValid(link)).isTrue();
		Assertions.assertThat(link.uri()).isEqualTo(URI.create("http://localhost/1"));
	}

	@Test
	public void link_with_param_empty_keept() {
		Link link = new Link().uri(URI.create("http://localhost/1?test"));
		filter.setExclusions(Arrays.asList("test"));
		Assertions.assertThat(filter.isValid(link)).isTrue();
		Assertions.assertThat(link.uri()).isEqualTo(URI.create("http://localhost/1?test"));
	}

	@Test
	public void link_with_params_all_filtered() {
		Link link = new Link().uri(URI.create("http://localhost/1?foo=test&foo2=test2"));
		Assertions.assertThat(filter.isValid(link)).isTrue();
		Assertions.assertThat(link.uri()).isEqualTo(URI.create("http://localhost/1"));
	}

	@Test
	public void link_with_params_and_keep_one() {
		Link link = new Link().uri(URI.create("http://localhost/1?foo=test&foo2=test2"));
		filter.setExclusions(Arrays.asList("foo2"));
		Assertions.assertThat(filter.isValid(link)).isTrue();
		Assertions.assertThat(link.uri()).isEqualTo(URI.create("http://localhost/1?foo2=test2"));
	}

	@Test
	public void link_with_params_and_keep_mutliple() {
		Link link = new Link().uri(URI.create("http://localhost/1?foo=test&foo2=test2&foo2=test3&z=13"));
		filter.setExclusions(Arrays.asList("foo2", "foo"));
		Assertions.assertThat(filter.isValid(link)).isTrue();
		Assertions.assertThat(link.uri()).isEqualTo(URI.create("http://localhost/1?foo=test&foo2=test2&foo2=test3"));
	}

}
