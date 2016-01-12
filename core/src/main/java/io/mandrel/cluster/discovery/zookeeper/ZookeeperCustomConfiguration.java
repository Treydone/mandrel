package io.mandrel.cluster.discovery.zookeeper;

import io.mandrel.common.thrift.ThriftServerProperties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceDiscovery;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperCustomConfiguration {

	@Autowired
	private CuratorFramework curator;

	@Autowired
	public ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;

	@Autowired
	private InstanceSerializer<ZookeeperInstance> instanceSerializer;

	@Autowired
	private ThriftServerProperties properties;

	@Autowired
	private ApplicationContext context;

	@Bean
	public ZookeeperServiceDiscovery zookeeperServiceDiscovery() {
		ZookeeperServiceDiscovery zookeeperServiceDiscovery = new ZookeeperServiceDiscovery(curator, zookeeperDiscoveryProperties, instanceSerializer);
		zookeeperServiceDiscovery.setApplicationContext(context);
		zookeeperServiceDiscovery.setPort(properties.getPort());
		zookeeperServiceDiscovery.build();
		return zookeeperServiceDiscovery;
	}
}
