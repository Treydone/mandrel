package io.mandrel.config;

import io.mandrel.common.settings.NetworkSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@Configuration
public class HazelcastConfiguration {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(HazelcastConfiguration.class);

	@Bean(destroyMethod = "shutdown")
	public HazelcastInstance hazelcastInstance(NetworkSettings networkSettings) {

		LOGGER.debug("Network settings: {}", networkSettings);

		Config config = new Config();

		// Group
		if (networkSettings.getGroup() != null) {
			config.setGroupConfig(new GroupConfig(networkSettings.getGroup()
					.getName(), networkSettings.getGroup().getPassword()));
		}

		// Network
		NetworkConfig networkConfig = config.getNetworkConfig();

		if (networkSettings.getInterfaces() != null) {
			InterfacesConfig interfaces = new InterfacesConfig();
			networkSettings.getInterfaces().forEach(
					nInterface -> interfaces.addInterface(nInterface));
			interfaces.setEnabled(true);
			networkConfig.setInterfaces(interfaces);
		}

		if (networkSettings.getTcp() != null) {
			networkConfig.setReuseAddress(networkSettings.getTcp()
					.isReuseAddress());
		}

		if (networkSettings.getDiscovery() != null) {

			JoinConfig join = networkConfig.getJoin();

			// Multicast
			if (networkSettings.getDiscovery().getMulticast() != null) {
				MulticastConfig multicastConfig = join.getMulticastConfig();
				multicastConfig.setEnabled(networkSettings.getDiscovery()
						.getMulticast().isEnabled());
				multicastConfig.setMulticastGroup(networkSettings
						.getDiscovery().getMulticast().getGroup());
				multicastConfig.setMulticastPort(networkSettings.getDiscovery()
						.getMulticast().getPort());
			}

			// Unicast
			if (networkSettings.getDiscovery().getUnicast() != null) {
				TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
				tcpIpConfig.setEnabled(networkSettings.getDiscovery()
						.getUnicast().isEnabled());
			}

			networkConfig.setJoin(join);
		}

		config.setNetworkConfig(networkConfig);

		// Start Hazelcast
		HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

		return instance;
	}
}
