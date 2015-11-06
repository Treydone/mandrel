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
package io.mandrel.requests.http.ua;

import io.mandrel.common.data.Spider;
import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.service.TaskContext;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class FixedUserAgentProvisionner extends UserAgentProvisionner {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class FixedUserAgentProvisionnerDefinition implements UserAgentProvisionnerDefinition, NamedDefinition, Serializable {
		private static final long serialVersionUID = -4024901085285125948L;

		@JsonProperty("ua")
		private String ua;

		@Override
		public FixedUserAgentProvisionner build(TaskContext context) {
			return new FixedUserAgentProvisionner(context).ua(ua);
		}

		@Override
		public String name() {
			return "fixed";
		}

		public FixedUserAgentProvisionnerDefinition(String ua) {
			this.ua = ua;
		}
	}

	public FixedUserAgentProvisionner(TaskContext context) {
		super(context);
	}

	private String ua;

	public String get(String url, Spider spider) {
		return ua;
	}
}
