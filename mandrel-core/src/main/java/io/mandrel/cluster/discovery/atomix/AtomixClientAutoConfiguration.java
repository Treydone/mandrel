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
package io.mandrel.cluster.discovery.atomix;

import io.atomix.Atomix;
import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.SneakyThrows;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "discovery.atomix.enabled", matchIfMissing = true)
@EnableConfigurationProperties
public class AtomixClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@SneakyThrows
	public Atomix atomix(AtomixDiscoveryProperties atomixDiscoveryProperties) {
		List<Address> members = atomixDiscoveryProperties.getHosts().stream().map(hap -> new Address(hap.getHostText(), hap.getPortOrDefault(50000)))
				.collect(Collectors.toList());
		AtomixClient client = AtomixClient.builder(members).withTransport(new NettyTransport()).build();
		CompletableFuture<Atomix> open = client.open();
		Atomix atomix = open.join();
		return atomix;
	}

}
