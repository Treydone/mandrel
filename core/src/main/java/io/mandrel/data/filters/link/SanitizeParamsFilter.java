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
package io.mandrel.data.filters.link;

import io.mandrel.common.net.Uri;
import io.mandrel.data.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SanitizeParamsFilter extends LinkFilter {

	private static final long serialVersionUID = -8284466714206360251L;

	private List<String> exclusions = new ArrayList<>();

	private final static Pattern QUERY = Pattern.compile("&");

	private final static Pattern PARAMS = Pattern.compile("=");

	public boolean isValid(Link link) {
		Uri linkUri = link.getUri();
		if (linkUri != null && StringUtils.isNotBlank(linkUri.toString())) {
			String uri = linkUri.toString();
			int pos = uri.indexOf('?');
			if (pos > -1) {
				String uriWithoutParams = uri.substring(0, pos);

				if (CollectionUtils.isNotEmpty(exclusions)) {
					String query = uri.substring(pos + 1, uri.length());

					if (StringUtils.isNotBlank(query)) {
						String[] paramPairs = QUERY.split(query);

						MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
						if (paramPairs != null) {
							for (String pair : paramPairs) {
								String[] paramValue = PARAMS.split(pair);
								if (exclusions.contains(paramValue[0])) {
									params.add(paramValue[0], paramValue.length > 1 ? paramValue[1] : null);
								}
							}
						}

						StringBuilder builder = new StringBuilder();
						params.entrySet().forEach(entry -> {
							entry.getValue().forEach(value -> {
								builder.append(entry.getKey());
								if (StringUtils.isNotBlank(value)) {
									builder.append("=").append(value);
								}
								builder.append("&");
							});
						});
						if (builder.length() > 0 && builder.charAt(builder.length() - 1) == '&') {
							builder.deleteCharAt(builder.length() - 1);
						}
						if (builder.length() > 0) {
							link.setUri(Uri.create(uriWithoutParams + "?" + builder.toString()));
						} else {
							link.setUri(Uri.create(uriWithoutParams));
						}
					} else {
						link.setUri(Uri.create(uriWithoutParams));
					}
				} else {
					link.setUri(Uri.create(uriWithoutParams));
				}
			}
		}
		return true;
	}

	@Override
	public String name() {
		return "sanitize_params";
	}
}
