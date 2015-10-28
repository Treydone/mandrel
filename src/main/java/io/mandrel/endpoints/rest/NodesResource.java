/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.endpoints.rest;

import io.mandrel.cluster.node.Node;
import io.mandrel.cluster.node.NodeService;
import io.mandrel.endpoints.contracts.NodesContract;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/nodes")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NodesResource implements NodesContract {

	private final NodeService nodeService;

	@ApiOperation(value = "List all the nodes", response = Node.class, responseContainer = "Map")
	public Map<URI, Node> all() {
		return nodeService.nodes();
	}

	@ApiOperation(value = "Find a node by its id", response = Node.class)
	public Node id(URI uri) {
		return nodeService.node(uri);
	}
}
