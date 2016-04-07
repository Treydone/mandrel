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
import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import io.mandrel.cluster.discovery.DiscoveryProperties;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.SneakyThrows;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(value = "discovery.atomix.enabled", matchIfMissing = true)
@EnableConfigurationProperties
public class AtomixServerAutoConfiguration {

	@Bean
	@Primary
	@SneakyThrows
	public Atomix atomix(AtomixDiscoveryProperties atomixDiscoveryProperties, DiscoveryProperties discoveryProperties) {
		Address address = new Address(discoveryProperties.getInstanceHost() == null ? AtomixDiscoveryClient.getIpAddress()
				: discoveryProperties.getInstanceHost(), 50000);
		List<Address> members = atomixDiscoveryProperties.getHosts().stream().map(hap -> new Address(hap.getHostText(), hap.getPortOrDefault(5000)))
				.collect(Collectors.toList());
		AtomixReplica replica = AtomixReplica.builder(address, members).withStorage(new Storage(StorageLevel.DISK)).withTransport(new NettyTransport()).build();
		CompletableFuture<Atomix> future = replica.open();
		Atomix atomix = future.join();
		return atomix;
	}
}
