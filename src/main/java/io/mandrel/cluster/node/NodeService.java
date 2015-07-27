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

import io.mandrel.cluster.discovery.DiscoveryService;
import io.mandrel.monitor.Infos;
import io.mandrel.timeline.NodeEvent;
import io.mandrel.timeline.NodeEvent.NodeEventType;
import io.mandrel.timeline.TimelineService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NodeService {

	public static final String NODES = "nodes";

	private final HazelcastInstance instance;

	private final DiscoveryService discoveryService;

	private final TimelineService timelineService;

	@PostConstruct
	public void init() {
		String uuid = discoveryService.dhis();
		instance.<String, Node> getMap(NODES).put(uuid, new Node().setUuid(uuid));
		timelineService.add(new NodeEvent().setNodeId(uuid).setType(NodeEventType.NODE_STARTED).setTime(LocalDateTime.now()));
	}

	@PreDestroy
	public void destroy() {
		timelineService.add(new NodeEvent().setNodeId(discoveryService.dhis()).setType(NodeEventType.NODE_STOPPED).setTime(LocalDateTime.now()));
	}

	public Map<String, Node> nodes() {
		List<String> uuids = discoveryService.all();
		return _nodes().entrySet().stream().filter(idNode -> uuids.contains(idNode.getKey()))
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
	}

	public Node node(String id) {
		return _nodes().get(id);
	}

	public void updateLocalNodeInfos(Infos infos) {
		String uuid = discoveryService.dhis();
		Node node = instance.<String, Node> getMap(NODES).get(uuid);
		if (node != null) {
			node.setInfos(infos);
			instance.<String, Node> getMap(NODES).put(uuid, node);
		}
	}

	public Node dhis() {
		return node(discoveryService.dhis());
	}

	public Map<String, Node> nodes(Collection<String> uuids) {
		return _nodes().entrySet().stream().filter(idNode -> uuids.contains(idNode.getKey()))
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
	}

	private IMap<String, Node> _nodes() {
		return instance.<String, Node> getMap(NODES);
	}
}
