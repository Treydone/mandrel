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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class SkipAncorFilter extends LinkFilter {

	private static final long serialVersionUID = -5195589618123470396L;

	public boolean isValid(Link link) {
		return link != null && StringUtils.isNotBlank(link.uri()) && (!link.uri().contains("#") || link.uri().contains("#/"));
	}

	@Override
	public String name() {
		return "skip_ancor";
	}
}
