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
import io.mandrel.controller.AdminClient;
import io.mandrel.timeline.NodeEvent;
import io.mandrel.timeline.NodeEvent.NodeEventType;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NodeService {

	private final DiscoveryClient discoveryClient;
	private final AdminClient adminClient;
	private final NodeRepository nodeRepository;

	@PostConstruct
	public void init() {
		discoveryClient
				.getInstances(ServiceIds.CONTROLLER)
				.stream()
				.findFirst()
				.ifPresent(
						si -> adminClient.add(new NodeEvent().setUri(discoveryClient.getLocalServiceInstance().getUri()).setType(NodeEventType.NODE_STARTED)
								.setTime(LocalDateTime.now()), si.getUri()));
	}

	@PreDestroy
	public void destroy() {
		discoveryClient
				.getInstances(ServiceIds.CONTROLLER)
				.stream()
				.findFirst()
				.ifPresent(
						si -> adminClient.add(new NodeEvent().setUri(discoveryClient.getLocalServiceInstance().getUri()).setType(NodeEventType.NODE_STOPPED)
								.setTime(LocalDateTime.now()), si.getUri()));
	}

	public Map<URI, Node> nodes() {
		return nodeRepository.findAll().stream().collect(Collectors.toMap(node -> node.uri(), node -> node));
	}

	public Node node(URI uri) {
		return nodeRepository.findOne(uri);
	}

	public Map<URI, Node> nodes(Collection<URI> uris) {
		return Lists.newArrayList(nodeRepository.findAll(uris)).stream().collect(Collectors.toMap(node -> node.uri(), node -> node));
	}
}
