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
import io.mandrel.data.filters.link.StartWithFilter;

import java.net.URI;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class StartWithFilterTest {

	private StartWithFilter filter = new StartWithFilter().setValue("http://localhost/1");

	@Test
	public void no_link() {
		Assertions.assertThat(filter.isValid(new Link())).isFalse();
	}

	@Test
	public void link_start_with_exact() {
		Assertions.assertThat(filter.isValid(new Link().uri(URI.create("http://localhost/1")))).isTrue();
	}

	@Test
	public void link_start_with_partially() {
		Assertions.assertThat(filter.isValid(new Link().uri(URI.create("http://localhost/1/other")))).isTrue();
	}

	@Test
	public void link_not_start_with() {
		Assertions.assertThat(filter.isValid(new Link().uri(URI.create("http://test/1")))).isFalse();
	}
}
