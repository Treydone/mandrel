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
package io.mandrel.data.content.selector;

import io.mandrel.metadata.FetchMetadata;
import io.mandrel.requests.http.Cookie;
import io.mandrel.requests.http.HttpFetchMetadata;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleCookieSelector extends CookieSelector<String> {

	public Instance<String> init(FetchMetadata data) {
		return new Instance<String>() {
			@Override
			public <T> List<T> select(String value, DataConverter<String, T> converter) {

				if (data instanceof HttpFetchMetadata) {
					List<Cookie> cookies = ((HttpFetchMetadata) data).cookies();
					return cookies.stream().filter(cookie -> cookie.name().equals(value)).map(cookie -> converter.convert(cookie.value()))
							.collect(Collectors.toList());
				}
				return null;
			}
		};
	}

	@Override
	public String name() {
		return "cookie";
	}
}
