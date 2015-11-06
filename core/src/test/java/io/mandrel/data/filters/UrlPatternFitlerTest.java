/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
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
import io.mandrel.data.filters.link.UrlPatternFilter;

import java.net.URI;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class UrlPatternFitlerTest {

	private UrlPatternFilter _static = UrlPatternFilter.STATIC;

	private UrlPatternFilter _custom = new UrlPatternFilter().setValue("http://localhost/users/(\\d+).html");

	private UrlPatternFilter _custom2 = new UrlPatternFilter().setValue("http://localhost/users/(\\d+).html(.*)");

	@Test
	public void static_no_link() {
		Assertions.assertThat(_static.isValid(new Link())).isFalse();
	}

	@Test
	public void static_link_simple() {
		Assertions.assertThat(_static.isValid(new Link().uri(URI.create("http://localhost/1")))).isTrue();
	}

	@Test
	public void static_link_static() {
		Assertions.assertThat(_static.isValid(new Link().uri(URI.create("http://localhost/1.jpg")))).isFalse();
	}

	@Test
	public void custom_link() {
		Assertions.assertThat(_custom.isValid(new Link().uri(URI.create("http://localhost/1.jpg")))).isFalse();
		Assertions.assertThat(_custom.isValid(new Link().uri(URI.create("http://localhost/users/1.jpg")))).isFalse();
		Assertions.assertThat(_custom.isValid(new Link().uri(URI.create("http://localhost/users/13455")))).isFalse();
		Assertions.assertThat(_custom.isValid(new Link().uri(URI.create("http://localhost/users/others/others")))).isFalse();
		Assertions.assertThat(_custom.isValid(new Link().uri(URI.create("http://localhost/users/1.html")))).isTrue();
		Assertions.assertThat(_custom.isValid(new Link().uri(URI.create("http://localhost/users/1.html?test")))).isFalse();

		Assertions.assertThat(_custom2.isValid(new Link().uri(URI.create("http://localhost/1.jpg")))).isFalse();
		Assertions.assertThat(_custom2.isValid(new Link().uri(URI.create("http://localhost/users/1.jpg")))).isFalse();
		Assertions.assertThat(_custom2.isValid(new Link().uri(URI.create("http://localhost/users/13455")))).isFalse();
		Assertions.assertThat(_custom2.isValid(new Link().uri(URI.create("http://localhost/users/others/others")))).isFalse();
		Assertions.assertThat(_custom2.isValid(new Link().uri(URI.create("http://localhost/users/1.html")))).isTrue();
		Assertions.assertThat(_custom2.isValid(new Link().uri(URI.create("http://localhost/users/1.html?test")))).isTrue();
	}
}
