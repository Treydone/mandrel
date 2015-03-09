package io.mandrel.rest;

import io.mandrel.service.node.Node;
import io.mandrel.service.node.NodeService;

import java.util.List;

import javax.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/nodes")
@RequestMapping(value = "/nodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class NodeResource {

	private final NodeService nodeService;

	@Inject
	public NodeResource(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@ApiOperation(value = "List all the nodes", response = Node.class, responseContainer = "List")
	@RequestMapping
	public List<Node> all() {
		return nodeService.all();
	}

	@ApiOperation(value = "Find a node by its id", response = Node.class)
	@RequestMapping(value = "/{id}")
	public Node id(@PathVariable String id) {
		return nodeService.id(id);
	}

	@ApiOperation(value = "Return the current node", response = Node.class)
	@RequestMapping(value = "/this")
	public Node dhis() {
		return nodeService.dhis();
	}
}
