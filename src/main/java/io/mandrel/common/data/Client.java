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

import io.mandrel.http.Cookie;
import io.mandrel.http.dns.CachedNameResolver;
import io.mandrel.http.dns.NameResolver;
import io.mandrel.http.proxy.NoProxyProxyServersSource;
import io.mandrel.http.proxy.ProxyServersSource;
import io.mandrel.http.ua.FixedUserAgentProvisionner;
import io.mandrel.http.ua.UserAgentProvisionner;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Client implements Serializable {

	private static final long serialVersionUID = -4242505953994309024L;

	@JsonProperty("request_time_out")
	private int requestTimeOut = 10000;

	@JsonProperty("headers")
	private Map<String, Collection<String>> headers;

	@JsonProperty("params")
	private Map<String, List<String>> params;

	@JsonProperty("follow_redirects")
	private boolean followRedirects = true;

	@JsonProperty("cookies")
	private List<Cookie> cookies;

	@JsonProperty("user_agent_provisionner")
	private UserAgentProvisionner userAgentProvisionner = new FixedUserAgentProvisionner("Mandrel");

	@JsonProperty("name_resolver")
	private NameResolver nameResolver = new CachedNameResolver();

	@JsonProperty("proxy")
	private ProxyServersSource proxyServersSource = new NoProxyProxyServersSource();

	@JsonProperty("politeness")
	private Politeness politeness = new Politeness();
}