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
package io.mandrel.common.data;

import io.mandrel.common.service.TaskContext;
import io.mandrel.requests.http.Cookie;
import io.mandrel.requests.http.ua.FixedUserAgentProvisionner.FixedUserAgentProvisionnerDefinition;
import io.mandrel.requests.http.ua.UserAgentProvisionner;
import io.mandrel.requests.http.ua.UserAgentProvisionner.UserAgentProvisionnerDefinition;

import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class HttpStrategy extends Strategy {

	public HttpStrategy(TaskContext context) {
		super(context);
	}

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class HttpStrategyDefinition extends StrategyDefinition<HttpStrategy> {
		private static final long serialVersionUID = -5847753994653490966L;

		@JsonProperty("max_redirects")
		private int maxRedirects = 3;

		@JsonProperty("follow_redirects")
		private boolean followRedirects = true;

		@JsonProperty("headers")
		private Set<Header> headers;

		@JsonProperty("params")
		private Set<Param> params;

		@JsonProperty("cookies")
		private List<Cookie> cookies;

		@JsonProperty("user_agent_provisionner")
		private UserAgentProvisionnerDefinition userAgentProvisionner = new FixedUserAgentProvisionnerDefinition("Mandrel");

		@Override
		public String name() {
			return "http";
		}

		@Override
		public HttpStrategy build(TaskContext context) {
			return build(new HttpStrategy(context).cookies(cookies).followRedirects(followRedirects).headers(headers).maxRedirects(maxRedirects).params(params)
					.userAgentProvisionner(userAgentProvisionner.build(context)), context);
		}
	}

	private int maxRedirects;
	private boolean followRedirects;
	private Set<Header> headers;
	private Set<Param> params;
	private List<Cookie> cookies;
	private UserAgentProvisionner userAgentProvisionner;

}
