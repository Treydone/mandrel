package io.mandrel.service.node;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.PathParam;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

@Component
public class NodeService {

	private final HazelcastInstance hazelcastInstance;

	private Function<? super Member, ? extends Node> mapper = member -> {
		Node node = new Node();
		node.setUuid(member.getUuid());
		node.setAttributes(member.getAttributes());
		return node;
	};

	@Inject
	public NodeService(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	public List<Node> all() {
		return hazelcastInstance.getCluster().getMembers().stream().map(mapper).collect(Collectors.toList());
	}

	public Node id(@PathParam("id") String id) {
		Optional<Member> memberById = hazelcastInstance.getCluster().getMembers().stream().filter(member -> id.equals(member.getUuid())).findFirst();

		return memberById.map(mapper).orElse(null);
	}

	public Node dhis() {
		Member localMember = hazelcastInstance.getCluster().getLocalMember();
		return mapper.apply(localMember);
	}
}
