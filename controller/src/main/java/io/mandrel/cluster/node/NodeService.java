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

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.cluster.discovery.ServiceInstance;
import io.mandrel.cluster.instance.StateService;
import io.mandrel.common.NotFoundException;
import io.mandrel.common.net.Uri;
import io.mandrel.common.sync.Container;
import io.mandrel.common.thrift.Clients;
import io.mandrel.common.thrift.Pooled;
import io.mandrel.endpoints.contracts.NodeContract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;

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
			List<ServiceInstance> instances = discoveryClient.getInstances(ServiceIds.node());

			List<Node> nodes = instances.stream().map(i -> {
				Pooled<NodeContract> pooled = clients.onNode(HostAndPort.fromParts(i.getHost(), i.getPort()));
				Node node = pooled.map(client -> client.dhis());
				node.setType(i.getName());
				return node;
			}).collect(Collectors.toList());
			nodeRepository.update(nodes);
		}
	}

	public Map<Uri, Node> nodes() {
		List<ServiceInstance> instances = discoveryClient.getInstances(ServiceIds.node());
		return nodes(instances.stream().map(si -> Uri.internal(si.getHost(), si.getPort())).collect(Collectors.toList()));
	}

	public Optional<Node> node(Uri uri) {
		return nodeRepository.get(Node.idOf(uri));
	}

	public List<Container> containers(String id) {
		return nodeRepository.get(id).map(node -> {
			Uri uri = Node.uriOf(id);
			HostAndPort hostAndPort = HostAndPort.fromParts(uri.getHost(), uri.getPort());

			List<Container> results = null;
			if (node.getType().equals("worker")) {
				results = clients.onWorker(hostAndPort).map(client -> client.listRunningContainers());
			} else if (node.getType().equals("frontier")) {
				results = clients.onFrontier(hostAndPort).map(client -> client.listRunningContainers());
			}
			return results;
		}).orElse(new ArrayList<>());
	}

	public Node node(String id) {
		return nodeRepository.get(id).orElseThrow(() -> new NotFoundException("Unknown node"));
	}

	public Map<Uri, Node> nodes(Collection<Uri> uris) {
		return Lists.newArrayList(nodeRepository.findAll(uris)).stream().collect(Collectors.toMap(node -> node.getUri(), node -> node));
	}
}
