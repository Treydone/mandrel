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

import io.mandrel.requests.http.Cookie;
import io.mandrel.requests.http.ua.FixedUserAgentProvisionner;
import io.mandrel.requests.http.ua.UserAgentProvisionner;

import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = false)
public class HttpStrategy extends Strategy {

	private static final long serialVersionUID = 8640333953772991190L;

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
	private UserAgentProvisionner userAgentProvisionner = new FixedUserAgentProvisionner("Mandrel");

}
