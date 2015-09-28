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

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
public class DNSNameResolver implements NameResolver {

	private static final long serialVersionUID = -623963740271067479L;

	private static final List<InetSocketAddress> DEFAULT_SERVERS = Arrays.asList(new InetSocketAddress("8.8.8.8", 53), // Google
																														// Public
																														// DNS
			new InetSocketAddress("8.8.4.4", 53), new InetSocketAddress("208.67.222.222", 53), // OpenDNS
			new InetSocketAddress("208.67.220.220", 53), new InetSocketAddress("37.235.1.174", 53), // FreeDNS
			new InetSocketAddress("37.235.1.177", 53));

	@JsonIgnore
	private EventLoopGroup group;

	@JsonIgnore
	private DnsNameResolver resolver;

	private List<InetSocketAddress> servers = DEFAULT_SERVERS;
	private int maxTriesPerQuery = DEFAULT_SERVERS.size();
	private int minTtl = Integer.MAX_VALUE;
	private int maxTtl = Integer.MAX_VALUE;

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

	@Override
	public String getType() {
		return "default";
	}
}
