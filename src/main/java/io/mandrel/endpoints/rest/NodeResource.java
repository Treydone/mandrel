package io.mandrel.endpoints.rest;

import io.mandrel.cluster.node.Node;
import io.mandrel.cluster.node.NodeService;

import java.util.Map;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NodeResource {

	private final NodeService nodeService;

	@ApiOperation(value = "List all the nodes", response = Node.class, responseContainer = "Map")
	@RequestMapping
	public Map<String, Node> all() {
		return nodeService.nodes();
	}

	@ApiOperation(value = "Find a node by its id", response = Node.class)
	@RequestMapping(value = "/{id}")
	public Node id(@PathVariable String id) {
		return nodeService.node(id);
	}

	@ApiOperation(value = "Return the current node", response = Node.class)
	@RequestMapping(value = "/this")
	public Node dhis() {
		return nodeService.dhis();
	}
}
