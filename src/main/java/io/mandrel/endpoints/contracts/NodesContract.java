package io.mandrel.endpoints.contracts;

import io.mandrel.cluster.node.Node;
import io.mandrel.endpoints.rest.Apis;

import java.net.URI;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(value = Apis.PREFIX + "/nodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
public interface NodesContract {

	@RequestMapping
	public Map<URI, Node> all();

	@RequestMapping(params = "uri")
	public Node id(@RequestParam URI uri);

}
