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
package io.mandrel.requests.proxy;

import io.mandrel.common.data.Job;
import io.mandrel.common.service.TaskContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class StaticProxyServersSource extends ProxyServersSource {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class StaticProxyServersSourceDefinition extends ProxyServersSourceDefinition<StaticProxyServersSource> {
		private static final long serialVersionUID = 4179034020754804054L;

		@JsonProperty("servers")
		private List<ProxyServer> servers = Arrays.asList((ProxyServer) null);

		@Override
		public StaticProxyServersSource build(TaskContext context) {
			return new StaticProxyServersSource(context).servers(servers);
		}

		@Override
		public String name() {
			return "static";
		}
	}

	public StaticProxyServersSource(TaskContext context) {
		super(context);
	}

	private List<ProxyServer> servers = Arrays.asList((ProxyServer) null);

	private AtomicInteger currentProxyServerIndex = new AtomicInteger();

	private Iterator<ProxyServer> roundRobin;

	public void init() {
		roundRobin = getProxyServersIterator(servers);
	}

	public ProxyServer findProxy(Job job) {
		return roundRobin.next();
	}

	public final Iterator<ProxyServer> getProxyServersIterator(final Collection<ProxyServer> servers) {
		int size = servers.size();
		if (size < 2) {
			this.getNextProxyServerStartIndex(size);
			return servers.iterator();
		}

		return this.buildProxyServerIterator(size, servers.toArray(new ProxyServer[size]));
	}

	private Iterator<ProxyServer> buildProxyServerIterator(int size, final ProxyServer[] servers) {

		int nextProxyServerStartIndex = getNextProxyServerStartIndex(size);

		final ProxyServer[] reorderedProxyServers = new ProxyServer[size];

		System.arraycopy(servers, nextProxyServerStartIndex, reorderedProxyServers, 0, size - nextProxyServerStartIndex);
		System.arraycopy(servers, 0, reorderedProxyServers, size - nextProxyServerStartIndex, 0 + nextProxyServerStartIndex);

		return new Iterator<ProxyServer>() {
			int currentIndex = 0;

			public boolean hasNext() {
				return currentIndex < reorderedProxyServers.length;
			}

			public ProxyServer next() {
				return reorderedProxyServers[currentIndex++];
			}

			public void remove() {
				throw new UnsupportedOperationException("Remove is not supported by this Iterator");
			}
		};
	}

	/**
	 * Keeps track of the last index over multiple dispatches. Each invocation
	 * of this method will increment the index by one, overflowing at
	 * <code>size</code>.
	 */
	private int getNextProxyServerStartIndex(int size) {
		if (size > 0) {
			int indexTail = currentProxyServerIndex.getAndIncrement() % size;
			return indexTail < 0 ? indexTail + size : indexTail;
		} else {
			return size;
		}
	}
}
