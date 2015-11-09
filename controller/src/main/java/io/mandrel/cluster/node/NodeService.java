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
import io.mandrel.cluster.instance.StateService;
import io.mandrel.common.client.Clients;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class NodeService {

	@Autowired
	private NodeRepository nodeRepository;
	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private Clients clients;
	@Autowired
	private StateService stateService;

	@Scheduled(fixedRate = 5000)
	public void sync() {
		if (stateService.isStarted()) {
			List<ServiceInstance> instances = allInstances();

			List<Node> nodes = instances.stream().map(i -> {
				return clients.nodeClient().dhis(i.getUri());
			}).collect(Collectors.toList());
			nodeRepository.update(nodes);
		}
	}

	public Map<URI, Node> nodes() {
		List<ServiceInstance> instances = allInstances();
		return nodes(instances.stream().map(si -> si.getUri()).collect(Collectors.toList()));
	}

	public Optional<Node> node(URI uri) {
		return nodeRepository.get(Node.idOf(uri));
	}

	public Optional<Node> node(String id) {
		return nodeRepository.get(id);
	}

	public Map<URI, Node> nodes(Collection<URI> uris) {
		return Lists.newArrayList(nodeRepository.findAll(uris)).stream().collect(Collectors.toMap(node -> node.getUri(), node -> node));
	}

	private List<ServiceInstance> allInstances() {
		List<ServiceInstance> instances = new ArrayList<>();
		instances.addAll(discoveryClient.getInstances(ServiceIds.CONTROLLER));
		instances.addAll(discoveryClient.getInstances(ServiceIds.FRONTIER));
		instances.addAll(discoveryClient.getInstances(ServiceIds.WORKER));
		return instances;
	}
}
