package io.mandrel.endpoints.contracts;

import io.mandrel.cluster.node.Node;
import io.mandrel.endpoints.rest.Apis;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = Apis.PREFIX + "/node", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
public interface NodeContract {

	@RequestMapping
	public Node dhis();
}
