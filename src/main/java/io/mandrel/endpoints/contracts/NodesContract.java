package io.mandrel.endpoints.contracts;

import io.mandrel.cluster.node.Node;
import io.mandrel.endpoints.rest.Apis;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/nodes")
@RequestMapping(value = Apis.PREFIX + "/nodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public interface NodesContract {

	@ApiOperation(value = "List all the nodes", response = Node.class, responseContainer = "Map")
	@RequestMapping
	public Map<String, Node> all();

	@ApiOperation(value = "Find a node by its id", response = Node.class)
	@RequestMapping(value = "/{id}")
	public Node id(@PathVariable String id);

}
