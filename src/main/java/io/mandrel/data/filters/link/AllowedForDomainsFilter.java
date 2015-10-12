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

import io.mandrel.data.spider.Link;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.collections.CollectionUtils;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class AllowedForDomainsFilter extends LinkFilter {

	private static final long serialVersionUID = -5195589618123470396L;

	private List<String> domains;

	public boolean isValid(Link link) {
		if (CollectionUtils.isNotEmpty(domains) && link != null && link.uri() != null) {
			return domains.stream().anyMatch(d -> link.uri().getHost().contains(d));
		}
		return false;
	}

	@Override
	public String name() {
		return "allowed_for_domains";
	}
}
