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
package io.mandrel.timeline;

import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.common.client.Clients;
import io.mandrel.timeline.NodeEvent.NodeEventType;

import java.time.LocalDateTime;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Component
public class EventSyncer implements ApplicationListener<ContextStartedEvent> {

	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private Clients clients;

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {
		discoveryClient
				.getInstances(ServiceIds.CONTROLLER)
				.stream()
				.findFirst()
				.ifPresent(
						si -> clients.controllerClient().add(
								new NodeEvent().setUri(discoveryClient.getLocalServiceInstance().getUri()).setType(NodeEventType.NODE_STARTED)
										.setTime(LocalDateTime.now()), si.getUri()));
	}

	@PreDestroy
	public void destroy() {
		discoveryClient
				.getInstances(ServiceIds.CONTROLLER)
				.stream()
				.findFirst()
				.ifPresent(
						si -> clients.controllerClient().add(
								new NodeEvent().setUri(discoveryClient.getLocalServiceInstance().getUri()).setType(NodeEventType.NODE_STOPPED)
										.setTime(LocalDateTime.now()), si.getUri()));
	}
}
