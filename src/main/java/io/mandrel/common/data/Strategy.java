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

import io.mandrel.requests.dns.DNSNameResolver;
import io.mandrel.requests.dns.NameResolver;
import io.mandrel.requests.proxy.NoProxyProxyServersSource;
import io.mandrel.requests.proxy.ProxyServersSource;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Strategy implements Serializable {

	private static final long serialVersionUID = 8944125504287219738L;

	@JsonProperty("request_time_out")
	private int requestTimeOut = 10000;

	@JsonProperty("socket_timeout")
	private int socketTimeout = 10000;

	@JsonProperty("connect_timeout")
	private int connectTimeout = 10000;

	@JsonProperty("reuse_address")
	private boolean reuseAddress = true;

	@JsonProperty("tcp_no_delay")
	private boolean tcpNoDelay = true;

	@JsonProperty("keep_alive")
	private boolean keepAlive = true;

	@JsonProperty("max_parallel")
	private int maxParallel = 100;

	@JsonProperty("max_persistent_connections")
	private int maxPersistentConnections = 100;

	@JsonProperty("name_resolver")
	private NameResolver nameResolver = new DNSNameResolver();

	@JsonProperty("proxy")
	private ProxyServersSource proxyServersSource = new NoProxyProxyServersSource();

}
