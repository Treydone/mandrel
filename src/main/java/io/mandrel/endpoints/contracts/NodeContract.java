package io.mandrel.endpoints.contracts;

import io.mandrel.cluster.node.Node;
import io.mandrel.endpoints.rest.Apis;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/nodes")
@RequestMapping(value = Apis.PREFIX + "/nodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public interface NodeContract {

	@ApiOperation(value = "Return the current node", response = Node.class)
	@RequestMapping(value = "/this")
	public Node dhis();
}

