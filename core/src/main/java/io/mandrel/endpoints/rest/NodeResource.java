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
import io.mandrel.endpoints.contracts.NodeContract;
import io.mandrel.monitor.Infos;
import io.mandrel.monitor.SigarService;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Slf4j
@RestController
@Api("/node")
public class NodeResource implements NodeContract {

	@Autowired
	private SigarService sigarService;
	@Autowired
	private DiscoveryClient discoveryClient;

	@ApiOperation(value = "Return the current node", response = Node.class)
	public Node dhis(URI target) {
		try {
			Infos infos = sigarService.infos();
			return new Node().infos(infos).uri(discoveryClient.getLocalServiceInstance().getUri());
		} catch (Exception e) {
			log.warn("Can not set the infos for the endpoint", e);
			throw Throwables.propagate(e);
		}
	}
}
