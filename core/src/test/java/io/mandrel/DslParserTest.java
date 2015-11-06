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
package io.mandrel;

import java.util.Arrays;

import io.mandrel.common.querydsl.DslParser;
import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.data.filters.link.SanitizeParamsFilter;
import io.mandrel.data.filters.link.StartWithFilter;
import io.mandrel.data.filters.link.UrlPatternFilter;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DslParserTest {

	@Test
	public void start_simple() {
		LinkFilter filter = DslParser.parseLinkFilter("start_with(value:'ertertert123!?;/')");
		Assertions.assertThat(filter).isInstanceOf(StartWithFilter.class)
				.isEqualToIgnoringGivenFields(new StartWithFilter().setValue("ertertert123!?;/"), "compiledPattern");
	}

	@Test
	public void start_with_spaces() {
		LinkFilter filter = DslParser.parseLinkFilter("start_with( value : 'ertertert123' )");
		Assertions.assertThat(filter).isInstanceOf(StartWithFilter.class)
				.isEqualToIgnoringGivenFields(new StartWithFilter().setValue("ertertert123"), "compiledPattern");
	}

	@Test
	public void pattern_simple() {
		LinkFilter filter = DslParser.parseLinkFilter("pattern(value:'ertertert123')");
		Assertions.assertThat(filter).isInstanceOf(UrlPatternFilter.class)
				.isEqualToIgnoringGivenFields(new UrlPatternFilter().setValue("ertertert123"), "compiledPattern");
	}

	@Test
	public void pattern_with_spaces() {
		LinkFilter filter = DslParser.parseLinkFilter("pattern( value    : 'ertertert123'  )");
		Assertions.assertThat(filter).isInstanceOf(UrlPatternFilter.class)
				.isEqualToIgnoringGivenFields(new UrlPatternFilter().setValue("ertertert123"), "compiledPattern");
	}

	@Test
	public void pattern_default() {
		LinkFilter filter = DslParser.parseLinkFilter("pattern('ertertert123')");
		Assertions.assertThat(filter).isInstanceOf(UrlPatternFilter.class)
				.isEqualToIgnoringGivenFields(new UrlPatternFilter().setValue("ertertert123"), "compiledPattern");
	}

	@Test
	public void sanitize_params_simple() {
		LinkFilter filter = DslParser.parseLinkFilter(" sanitize_params(exclusions: ['ertertert123',    'ert' ])");
		Assertions.assertThat(filter).isInstanceOf(SanitizeParamsFilter.class)
				.isEqualToComparingFieldByField(new SanitizeParamsFilter().setExclusions(Arrays.asList("ertertert123", "ert")));
	}

	@Test
	public void composite() {
		DslParser
				.parseLinkFilter("start_with(value:'qsd') or pattern('ertertert') and (start_with)     and (start_with(value:'pouet') or start_with(value:'bracket'))");
	}
}
