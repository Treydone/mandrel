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
package io.mandrel.endpoints.internal;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.node.Node;
import io.mandrel.common.net.Uri;
import io.mandrel.common.settings.InfoSettings;
import io.mandrel.endpoints.contracts.NodeContract;
import io.mandrel.monitor.Infos;
import io.mandrel.monitor.SigarService;
import io.mandrel.transport.TransportProperties;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;

@Slf4j
@Component
public class NodeResource implements NodeContract {

	@Autowired
	private SigarService sigarService;
	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private TransportProperties properties;
	@Autowired
	private InfoSettings settings;

	public Node dhis() {
		try {
			Infos infos = sigarService.infos();
			return new Node().setInfos(infos).setUri(Uri.internal(discoveryClient.getInstanceHost(), properties.getPort())).setVersion(settings.getVersion());
		} catch (Exception e) {
			log.warn("Can not set the infos for the endpoint", e);
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void close() throws Exception {

	}
}
