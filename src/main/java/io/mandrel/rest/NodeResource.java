package io.mandrel.rest;

import io.mandrel.service.node.Node;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/node")
@Path("/node")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class NodeResource {

	private final HazelcastInstance hazelcastInstance;

	private Function<? super Member, ? extends Node> mapper = member -> {
		Node node = new Node();
		node.setUuid(member.getUuid());
		node.setAttributes(member.getAttributes());
		return node;
	};

	@Inject
	public NodeResource(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	@ApiOperation(value = "List all the nodes", response = Node.class, responseContainer = "List")
	@Path("/all")
	@GET
	public List<Node> all() {
		return hazelcastInstance.getCluster().getMembers().stream().map(mapper)
				.collect(Collectors.toList());
	}

	@ApiOperation(value = "Find a node by its id", response = Node.class)
	@Path("/{id}")
	@GET
	public Node id(@PathParam("id") String id) {
		Optional<Member> memberById = hazelcastInstance.getCluster()
				.getMembers().stream()
				.filter(member -> id.equals(member.getUuid())).findFirst();

		return memberById.map(mapper).orElse(null);
	}

	@ApiOperation(value = "Return the current node", response = Node.class)
	@Path("/this")
	@GET
	public Node dhis() {
		Member localMember = hazelcastInstance.getCluster().getLocalMember();
		return mapper.apply(localMember);
	}
}
