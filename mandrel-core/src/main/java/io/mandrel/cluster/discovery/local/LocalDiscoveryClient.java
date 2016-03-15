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
package io.mandrel.cluster.discovery.local;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.DiscoveryProperties;
import io.mandrel.cluster.discovery.ServiceInstance;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@ConditionalOnProperty(value = "discovery.local.enabled", matchIfMissing = false)
@Component
public class LocalDiscoveryClient implements DiscoveryClient {

	private final static String ID = UUID.randomUUID().toString();

	@Autowired
	private DiscoveryProperties discoveryProperties;

	private ConcurrentMap<String, ServiceInstance> services = new ConcurrentHashMap<>();

	@Override
	public ServiceInstance register(ServiceInstance instance) {
		ServiceInstance finalInstance = ServiceInstance.builder().port(instance.getPort())
				.host(instance.getHost() != null ? instance.getHost() : getInstanceHost()).name(instance.getName()).id(ID).build();
		services.put(instance.getName(), finalInstance);
		return finalInstance;
	}

	@Override
	public void unregister(String serviceId) {
		services.remove(serviceId);
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		return Arrays.asList(services.get(serviceId));
	}

	@Override
	public List<String> getServices() {
		return Lists.newArrayList(services.keySet());
	}

	@Override
	public String getInstanceHost() {
		String host = discoveryProperties.getInstanceHost() == null ? "localhost" : discoveryProperties.getInstanceHost();
		return host;
	}

	@Override
	public ServiceInstance getLocalInstance(String serviceId) {
		return services.get(serviceId);
	}

	@Override
	public ServiceInstance getInstance(String id, String serviceId) {
		return services.get(serviceId);
	}

	@Override
	public String getInstanceId() {
		return ID;
	}

}
