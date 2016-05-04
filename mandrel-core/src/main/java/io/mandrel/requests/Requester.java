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
package io.mandrel.requests;

import io.mandrel.blob.Blob;
import io.mandrel.common.data.Job;
import io.mandrel.common.lifecycle.Initializable;
import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.ObjectFactory;
import io.mandrel.common.service.TaskContext;
import io.mandrel.common.service.TaskContextAware;
import io.mandrel.monitor.health.Checkable;
import io.mandrel.requests.dns.DNSNameResolver.DNSNameResolverDefinition;
import io.mandrel.requests.dns.NameResolver;
import io.mandrel.requests.dns.NameResolver.NameResolverDefinition;
import io.mandrel.requests.proxy.NoProxyProxyServersSource.NoProxyProxyServersSourceDefinition;
import io.mandrel.requests.proxy.ProxyServersSource;
import io.mandrel.requests.proxy.ProxyServersSource.ProxyServersSourceDefinition;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true, fluent = true)
public abstract class Requester extends TaskContextAware implements Checkable, Initializable, Closeable {

	public Requester(TaskContext context) {
		super(context);
	}

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static abstract class RequesterDefinition<REQUESTER extends Requester> implements NamedDefinition, ObjectFactory<REQUESTER>, Serializable {
		private static final long serialVersionUID = 8753562363550894996L;

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
		private NameResolverDefinition<? extends NameResolver> nameResolver = new DNSNameResolverDefinition();

		@JsonProperty("proxy")
		private ProxyServersSourceDefinition<? extends ProxyServersSource> proxyServersSource = new NoProxyProxyServersSourceDefinition();

		protected REQUESTER build(REQUESTER requester, TaskContext context) {
			requester.connectTimeout(connectTimeout).requestTimeOut(requestTimeOut).socketTimeout(socketTimeout).reuseAddress(reuseAddress)
					.keepAlive(keepAlive).maxParallel(maxParallel).maxPersistentConnections(maxPersistentConnections).nameResolver(nameResolver.build(context))
					.proxyServersSource(proxyServersSource.build(context));
			return requester;
		}
	}

	protected int requestTimeOut;
	protected int socketTimeout;
	protected int connectTimeout;
	protected boolean reuseAddress;
	protected boolean tcpNoDelay;
	protected boolean keepAlive;
	protected int maxParallel;
	protected int maxPersistentConnections;
	protected NameResolver nameResolver;
	protected ProxyServersSource proxyServersSource;

	public abstract Blob get(Uri uri, Job job) throws Exception;

	public abstract Blob get(Uri uri) throws Exception;

	public abstract Set<String> getProtocols();
}
