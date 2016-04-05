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
import io.mandrel.common.sync.Container;
import io.mandrel.endpoints.contracts.coordinator.NodesContract;
import io.mandrel.transport.MandrelClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.weakref.jmx.internal.guava.collect.Maps;

import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NodeService implements NodesContract {

	private final NodeRepository nodeRepository;
	private final DiscoveryClient discoveryClient;
	private final MandrelClient client;
	private final StateService stateService;

	@Scheduled(fixedRate = 5000)
	public void sync() {
		if (stateService.isStarted()) {
			List<ServiceInstance> instances = discoveryClient.getInstances(ServiceIds.node());

			List<Node> nodes = instances.stream().map(i -> {
				Node node = client.node().on(i.getHostAndPort()).map(client -> client.dhis());
				node.setId(i.getId()).setHostAndPort(i.getHostAndPort());
				return node;
			}).collect(Collectors.toList());
			nodeRepository.update(nodes);
		}
	}

	public Map<String, Node> getNodes() {
		List<ServiceInstance> instances = discoveryClient.getInstances(ServiceIds.node());
		return getNodes(instances.stream().map(si -> si.getId()).collect(Collectors.toList()));
	}

	public Map<String, List<Container>> getContainers(String id) {
		return nodeRepository.get(id).map(node -> {
			HostAndPort hostAndPort = node.getHostAndPort();

			Map<String, List<Container>> results = Maps.newHashMap();
			if (discoveryClient.getInstance(id, ServiceIds.worker()) != null) {
				results.put(ServiceIds.worker(), client.worker().admin().on(hostAndPort).map(client -> client.listRunningWorkerContainers()));
			}
			if (discoveryClient.getInstance(id, ServiceIds.frontier()) != null) {
				results.put(ServiceIds.frontier(), client.frontier().admin().on(hostAndPort).map(client -> client.listRunningFrontierContainers()));
			}
			return results;
		}).orElse(Maps.newHashMap());
	}

	public Node getNode(String id) {
		return nodeRepository.get(id).orElseThrow(() -> new NotFoundException("Unknown node"));
	}

	public Map<String, Node> getNodes(Collection<String> ids) {
		return Lists.newArrayList(nodeRepository.findAll(ids)).stream().collect(Collectors.toMap(node -> node.getId(), node -> node));
	}

	@Override
	public void close() throws Exception {

	}
}
