package io.mandrel.cluster.node;

import io.mandrel.cluster.discovery.DiscoveryService;
import io.mandrel.monitor.Infos;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		node.setInfos(infos);
		instance.<String, Node> getMap(NODES).put(uuid, node);
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
