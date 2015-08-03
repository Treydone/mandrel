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
package io.mandrel.data.filters.page;

import io.mandrel.http.WebPage;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

public interface BooleanWebPageFilters {

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class NotFilter extends WebPageFilter {

		private static final long serialVersionUID = -2429186996142024643L;

		private WebPageFilter filter;

		public boolean isValid(WebPage webPage) {
			return !filter.isValid(webPage);
		}

		@Override
		public String getType() {
			return "not";
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class OrFilter extends WebPageFilter {

		private static final long serialVersionUID = 7721027082341003067L;

		private List<WebPageFilter> filters;

		public boolean isValid(WebPage webPage) {
			return filters.stream().anyMatch(f -> f.isValid(webPage));
		}

		@Override
		public String getType() {
			return "or";
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class AndFilter extends WebPageFilter {

		private static final long serialVersionUID = -7125723269925872394L;

		private List<WebPageFilter> filters;

		public boolean isValid(WebPage webPage) {
			return filters.stream().allMatch(f -> f.isValid(webPage));
		}

		@Override
		public String getType() {
			return "and";
		}
	}
}
