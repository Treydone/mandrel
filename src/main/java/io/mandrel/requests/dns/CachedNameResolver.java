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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Throwables;
import com.google.common.net.InetAddresses;

@Data
@Slf4j
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class CachedNameResolver extends NameResolver {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class CachedNameResolverDefinition extends NameResolverDefinition<CachedNameResolver> {
		private static final long serialVersionUID = 4179034020754804054L;

		@JsonProperty("addresses")
		private Map<String, String> addresses = new HashMap<>();

		@Override
		public CachedNameResolver build(TaskContext context) {
			return new CachedNameResolver(context).addresses(addresses.entrySet().stream()
					.map(entry -> Pair.of(entry.getKey(), InetAddresses.forString(entry.getValue())))
					.collect(Collectors.toMap(p -> p.getLeft(), p -> p.getRight())));
		}

		@Override
		public String name() {
			return "cached";
		}
	}

	public CachedNameResolver(TaskContext context) {
		super(context);
	}

	private Map<String, InetAddress> addresses;

	public InetAddress resolve(String name) throws UnknownHostException {
		return name != null ? addresses.computeIfAbsent(name, key -> {
			try {
				return InetAddress.getByName(key);
			} catch (UnknownHostException e) {
				log.debug("", e);
				throw Throwables.propagate(e);
			}
		}) : null;
	}

	public void init() {
	}
}
