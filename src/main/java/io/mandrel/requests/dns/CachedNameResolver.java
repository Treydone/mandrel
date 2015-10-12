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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Throwables;

@Data
@Slf4j
public class CachedNameResolver implements NameResolver {

	private static final long serialVersionUID = 4179034020754804054L;

	private ConcurrentMap<String, InetAddress> addresses = new ConcurrentHashMap<>();

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

	@Override
	public String name() {
		return "cached";
	}
}
