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
package io.mandrel.requests.dns;

import io.mandrel.common.service.TaskContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsServerAddresses;
import io.netty.util.internal.ThreadLocalRandom;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class DNSNameResolver extends NameResolver {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class DNSNameResolverDefinition extends NameResolverDefinition<DNSNameResolver> {
		private static final long serialVersionUID = -2800579764535044200L;

		private static final List<String> DEFAULT_SERVERS = Arrays.asList("37.235.1.177:53", "8.8.8.8:53", // Google
				"8.8.4.4:53", "208.67.222.222:53", // OpenDNS
				"208.67.220.220:53", "37.235.1.174:53" // FreeDNS
		);

		@JsonProperty("servers")
		private List<String> servers = DEFAULT_SERVERS;

		@JsonProperty("max_tries_per_query")
		private int maxTriesPerQuery = DEFAULT_SERVERS.size();

		@JsonProperty("min_ttl")
		private int minTtl = Integer.MAX_VALUE;

		@JsonProperty("max_ttl")
		private int maxTtl = Integer.MAX_VALUE;

		@Override
		public DNSNameResolver build(TaskContext context) {
			Splitter splitter = Splitter.on(":");
			return new DNSNameResolver(context).maxTriesPerQuery(maxTriesPerQuery).maxTtl(maxTtl).minTtl(minTtl).servers(servers.stream().map(add -> {
				List<String> split = Lists.newArrayList(splitter.split(add));
				try {
					return new InetSocketAddress(InetAddresses.forString(split.get(0)), split.size() == 2 ? Integer.valueOf(split.get(1)) : 53);
				} catch (Exception e) {
					throw Throwables.propagate(e);
				}
			}).collect(Collectors.toList()));
		}

		@Override
		public String name() {
			return "default";
		}
	}

	public DNSNameResolver(TaskContext context) {
		super(context);
	}

	private List<InetSocketAddress> servers;
	private int maxTriesPerQuery;
	private int minTtl;
	private int maxTtl;

	private EventLoopGroup group;
	private DnsNameResolver resolver;

	public void init() {
		group = new NioEventLoopGroup(1);
		resolver = new DnsNameResolver(group.next(), NioDatagramChannel.class, DnsServerAddresses.shuffled(servers));
		resolver.setMaxTriesPerQuery(maxTriesPerQuery);
		resolver.setTtl(minTtl, maxTtl);
	}

	public InetAddress resolve(String name) throws UnknownHostException {
		InetSocketAddress unresolved = InetSocketAddress.createUnresolved(name, ThreadLocalRandom.current().nextInt(65536));
		try {
			return resolver.resolve(unresolved).get().getAddress();
		} catch (InterruptedException | ExecutionException e) {
			UnknownHostException unknownHostException = new UnknownHostException(name);
			unknownHostException.setStackTrace(e.getStackTrace());
			throw unknownHostException;
		}
	}
}
