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
package io.mandrel.config;

import io.mandrel.common.serialization.CompressionType;
import io.mandrel.common.serialization.KryoSerializer;
import io.mandrel.common.settings.NetworkSettings;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.hazelcast.config.Config;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.context.SpringManagedContext;

@Configuration
@Slf4j
public class HazelcastConfiguration {

	@Bean
	public SpringManagedContext context() {
		return new SpringManagedContext();
	}

	@Bean(destroyMethod = "shutdown")
	public HazelcastInstance hazelcastInstance(NetworkSettings networkSettings, KryoPool pool) {

		log.debug("Network settings: {}", networkSettings);

		Config config = new Config();
		config.setManagedContext(context());

		// Group
		if (networkSettings.getGroup() != null) {
			config.setGroupConfig(new GroupConfig(networkSettings.getGroup().getName(), networkSettings.getGroup().getPassword()));
		}

		// Network
		NetworkConfig networkConfig = config.getNetworkConfig();

		if (networkSettings.getInterfaces() != null) {
			InterfacesConfig interfaces = new InterfacesConfig();
			networkSettings.getInterfaces().forEach(nInterface -> interfaces.addInterface(nInterface));
			interfaces.setEnabled(true);
			networkConfig.setInterfaces(interfaces);
		}

		networkConfig.setReuseAddress(networkSettings.isReuseAddress());

		if (networkSettings.getDiscovery() != null) {

			JoinConfig join = networkConfig.getJoin();

			// Multicast
			if (networkSettings.getDiscovery().getMulticast() != null) {
				MulticastConfig multicastConfig = join.getMulticastConfig();
				multicastConfig.setEnabled(networkSettings.getDiscovery().getMulticast().isEnabled());
				multicastConfig.setMulticastGroup(networkSettings.getDiscovery().getMulticast().getGroup());
				multicastConfig.setMulticastPort(networkSettings.getDiscovery().getMulticast().getPort());
				multicastConfig.setMulticastTimeToLive(networkSettings.getDiscovery().getMulticast().getTimeToLive());
				multicastConfig.setMulticastTimeoutSeconds(networkSettings.getDiscovery().getMulticast().getTimeout());
			}

			// Unicast
			if (networkSettings.getDiscovery().getUnicast() != null) {
				TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
				tcpIpConfig.setEnabled(networkSettings.getDiscovery().getUnicast().isEnabled());
				tcpIpConfig.setMembers(networkSettings.getDiscovery().getUnicast().getMembers());
				tcpIpConfig.setRequiredMember(networkSettings.getDiscovery().getUnicast().getRequiredMember());
				tcpIpConfig.setConnectionTimeoutSeconds(networkSettings.getDiscovery().getUnicast().getConnectionTimeout());
			}

			networkConfig.setJoin(join);
		}

		config.setNetworkConfig(networkConfig);

		GlobalSerializerConfig global = new GlobalSerializerConfig().setImplementation(new KryoSerializer<>(CompressionType.SNAPPY, Object.class, pool, 0));
		config.getSerializationConfig().setGlobalSerializerConfig(global);

		// Start Hazelcast
		HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

		return instance;
	}
}
