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
package io.mandrel.cluster.discovery.zookeeper;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;
import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.DiscoveryProperties;
import io.mandrel.cluster.discovery.ServiceInstance;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import lombok.SneakyThrows;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.UriSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(value = "discovery.zookeeper.enabled", matchIfMissing = true)
@Component
public class ZookeeperDiscoveryClient implements DiscoveryClient {

	@Autowired
	private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;
	@Autowired
	private DiscoveryProperties discoveryProperties;
	@Autowired
	private ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;
	@Autowired
	private ApplicationContext context;

	private final static String ID = UUID.randomUUID().toString();

	@Override
	@SneakyThrows
	public ServiceInstance register(ServiceInstance instance) {
		org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> service = createService(instance).id(ID).build();
		serviceDiscovery.registerService(service);
		return create(service);
	}

	protected ServiceInstanceBuilder<ZookeeperInstance> createService(ServiceInstance instance) throws Exception {
		String host = getInstanceHost();

		ServiceInstanceBuilder<ZookeeperInstance> service = org.apache.curator.x.discovery.ServiceInstance.<ZookeeperInstance> builder()
				.name(instance.getName()).payload(new ZookeeperInstance(context.getId())).port(instance.getPort()).address(host)
				.uriSpec(new UriSpec(zookeeperDiscoveryProperties.getUriSpec()));
		return service;
	}

	@Override
	public String getInstanceHost() {
		String host = discoveryProperties.getInstanceHost() == null ? getIpAddress() : discoveryProperties.getInstanceHost();
		return host;
	}

	@Override
	public String getInstanceId() {
		return ID;
	}

	@Override
	@SneakyThrows
	public void unregister(String serviceId) {
		org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> service = org.apache.curator.x.discovery.ServiceInstance
				.<ZookeeperInstance> builder().name(serviceId).id(ID).build();
		serviceDiscovery.unregisterService(service);
	}

	@Override
	@SneakyThrows
	public ServiceInstance getLocalInstance(String serviceId) {
		org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> zkInstance = serviceDiscovery.queryForInstance(serviceId, ID);
		return create(zkInstance);
	}

	@Override
	@SneakyThrows
	public List<ServiceInstance> getInstances(String serviceId) {
		Collection<org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance>> zkInstances = serviceDiscovery.queryForInstances(serviceId);

		List<ServiceInstance> instances = new ArrayList<>(zkInstances.size());

		for (org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> instance : zkInstances) {
			instances.add(create(instance));
		}

		return instances;
	}

	@Override
	@SneakyThrows
	public ServiceInstance getInstance(String id, String serviceId) {
		org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> zkInstance = serviceDiscovery.queryForInstance(serviceId, id);
		return create(zkInstance);
	}

	protected ServiceInstance create(org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> instance) {
		if (instance != null) {
			return ServiceInstance.builder().host(instance.getAddress()).port(instance.getPort()).name(instance.getName()).id(instance.getId()).build();
		}
		return null;
	}

	@Override
	public List<String> getServices() {
		List<String> services = null;
		try {
			services = new ArrayList<>(serviceDiscovery.queryForNames());
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
		return services;
	}

	/**
	 * Return a non loopback IPv4 address for the machine running this process.
	 * If the machine has multiple network interfaces, the IP address for the
	 * first interface returned by
	 * {@link java.net.NetworkInterface#getNetworkInterfaces} is returned.
	 *
	 * @return non loopback IPv4 address for the machine running this process
	 * @see java.net.NetworkInterface#getNetworkInterfaces
	 * @see java.net.NetworkInterface#getInetAddresses
	 */
	public static String getIpAddress() {
		try {
			for (Enumeration<NetworkInterface> enumNic = NetworkInterface.getNetworkInterfaces(); enumNic.hasMoreElements();) {
				NetworkInterface ifc = enumNic.nextElement();
				if (ifc.isUp()) {
					for (Enumeration<InetAddress> enumAddr = ifc.getInetAddresses(); enumAddr.hasMoreElements();) {
						InetAddress address = enumAddr.nextElement();
						if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
							return address.getHostAddress();
						}
					}
				}
			}
		} catch (IOException e) {
			// ignore
		}
		return "unknown";
	}
}
