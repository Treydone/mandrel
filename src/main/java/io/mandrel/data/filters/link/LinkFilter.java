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

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = BooleanLinkFilters.AndFilter.class, name = "and"), @Type(value = BooleanLinkFilters.OrFilter.class, name = "or"),
		@Type(value = BooleanLinkFilters.NotFilter.class, name = "not"), @Type(value = AllowedForDomainsFilter.class, name = "allowed_for_domains"),
		@Type(value = SkipAncorFilter.class, name = "skip_ancor"), @Type(value = UrlPatternFilter.class, name = "pattern"),
		@Type(value = StartWithFilter.class, name = "start_with"), @Type(value = SanitizeParamsFilter.class, name = "sanitize_params") })
@Data
public abstract class LinkFilter implements Serializable {

	private static final long serialVersionUID = 4415317526564869848L;

	public abstract String getType();

	public abstract boolean isValid(Link link);
}
