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
package io.mandrel.cluster.node;

import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.common.client.Clients;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class NodeService implements ApplicationListener<ContextStartedEvent> {

	@Autowired
	private NodeRepository nodeRepository;
	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private Clients clients;

	private final AtomicBoolean started = new AtomicBoolean();

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {
		started.compareAndSet(true, true);
	}

	@Scheduled(fixedRate = 5000)
	public void sync() {
		if (started.get()) {
			List<ServiceInstance> instances = new ArrayList<>();
			instances.addAll(discoveryClient.getInstances(ServiceIds.CONTROLLER));
			instances.addAll(discoveryClient.getInstances(ServiceIds.FRONTIER));
			instances.addAll(discoveryClient.getInstances(ServiceIds.WORKER));

			List<Node> nodes = instances.stream().map(i -> clients.commonClient().dhis(i.getUri())).collect(Collectors.toList());
			nodeRepository.update(nodes);
		}
	}

	public Map<URI, Node> nodes() {
		return Lists.newArrayList(nodeRepository.findAll()).stream().collect(Collectors.toMap(node -> node.uri(), node -> node));
	}

	public Optional<Node> node(URI uri) {
		return nodeRepository.get(uri);
	}

	public Map<URI, Node> nodes(Collection<URI> uris) {
		return Lists.newArrayList(nodeRepository.findAll(uris)).stream().collect(Collectors.toMap(node -> node.uri(), node -> node));
	}
}
