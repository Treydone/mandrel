package io.mandrel.rest;

import io.mandrel.service.node.Node;
import io.mandrel.service.node.NodeService;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/node")
@Path("/node")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class NodeResource {

	private final NodeService nodeService;

	@Inject
	public NodeResource(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@ApiOperation(value = "List all the nodes", response = Node.class, responseContainer = "List")
	@Path("/all")
	@GET
	public List<Node> all() {
		return nodeService.all();
	}

	@ApiOperation(value = "Find a node by its id", response = Node.class)
	@Path("/{id}")
	@GET
	public Node id(@PathParam("id") String id) {
		return nodeService.id(id);
	}

	@ApiOperation(value = "Return the current node", response = Node.class)
	@Path("/this")
	@GET
	public Node dhis() {
		return nodeService.dhis();
	}
}
