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
package io.mandrel.transport;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.ServiceInstance;
import io.mandrel.common.MandrelException;
import io.mandrel.endpoints.contracts.Contract;
import io.mandrel.transport.thrift.KeyedClientPool;

import java.util.Optional;
import java.util.stream.Stream;

import lombok.Data;

import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

@Data
public class Targeted<T extends Contract & AutoCloseable> {

	private final DiscoveryClient discoveryClient;
	private final String serviceId;
	private final KeyedClientPool<T> pool;

	public Pooled<T> on(HostAndPort hostAndPort) {
		return pool.get(hostAndPort);
	}

	public Pooled<T> onAny() {
		Optional<ServiceInstance> opInstance = discoveryClient.getInstances(serviceId).stream().findFirst();
		if (opInstance.isPresent()) {
			try {
				ServiceInstance instance = opInstance.get();
				return pool.get(instance.getHostAndPort());
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		} else {
			throw new MandrelException("No frontier found");
		}
	}

	public Stream<Pooled<T>> onAll() {
		return discoveryClient.getInstances(serviceId).stream().map(service -> pool.get(service.getHostAndPort()));
	}
}
