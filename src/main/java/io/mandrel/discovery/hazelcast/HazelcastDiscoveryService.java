package io.mandrel.discovery.hazelcast;

import io.mandrel.discovery.DiscoveryService;
import io.mandrel.node.Node;
import io.mandrel.node.NodeService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

@Service
public class HazelcastDiscoveryService implements DiscoveryService {

	private final HazelcastInstance hazelcastInstance;

	private final NodeService nodeService;

	private final Function<? super Member, ? extends Node> mapper = kv -> {
		return nodeService.node(kv.getUuid());
	};

	@Inject
	public HazelcastDiscoveryService(HazelcastInstance hazelcastInstance, NodeService nodeService) {
		this.hazelcastInstance = hazelcastInstance;
		this.nodeService = nodeService;
	}

	public List<Node> all() {
		Set<Member> members = hazelcastInstance.getCluster().getMembers();
		return members.stream().map(mapper).collect(Collectors.toList());
	}

	public Node id(String id) {
		Optional<Member> result = hazelcastInstance.getCluster().getMembers().stream().filter(member -> id.equals(member.getUuid())).findFirst();
		return result.map(mapper).orElseThrow(() -> new RuntimeException("Node unknown"));
	}

	public Node dhis() {
		return id(hazelcastInstance.getCluster().getLocalMember().getUuid());
	}

}
