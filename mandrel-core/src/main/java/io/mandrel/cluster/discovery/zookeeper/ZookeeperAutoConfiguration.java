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
package io.mandrel.cluster.discovery.zookeeper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.RetryPolicy;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "discovery.zookeeper.enabled", matchIfMissing = false)
@EnableConfigurationProperties
@Slf4j
public class ZookeeperAutoConfiguration {

	@Autowired(required = false)
	private EnsembleProvider ensembleProvider;

	@Autowired
	private ZookeeperDiscoveryProperties zookeeperProperties;

	@Bean
	public InstanceSerializer<ZookeeperInstance> instanceSerializer() {
		return new JsonInstanceSerializer<>(ZookeeperInstance.class);
	}

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	@SneakyThrows
	public CuratorFramework curatorFramework(RetryPolicy retryPolicy, ZookeeperDiscoveryProperties properties) {
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
		if (ensembleProvider != null) {
			builder.ensembleProvider(ensembleProvider);
		}
		CuratorFramework curator = builder.retryPolicy(retryPolicy).connectString(zookeeperProperties.getConnectString()).build();
		curator.start();

		log.trace("blocking until connected to zookeeper for " + properties.getBlockUntilConnectedWait() + properties.getBlockUntilConnectedUnit());
		curator.blockUntilConnected(properties.getBlockUntilConnectedWait(), properties.getBlockUntilConnectedUnit());
		log.trace("connected to zookeeper");
		return curator;
	}

	@Bean
	@SneakyThrows
	public ServiceDiscovery<ZookeeperInstance> serviceDiscovery(CuratorFramework curatorFramework, InstanceSerializer<ZookeeperInstance> instanceSerializer,
			ZookeeperDiscoveryProperties zookeeperProperties) {
		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = ServiceDiscoveryBuilder.builder(ZookeeperInstance.class).client(curatorFramework)
				.basePath(zookeeperProperties.getRoot()).serializer(instanceSerializer).build();
		serviceDiscovery.start();
		return serviceDiscovery;
	}

	@Bean
	@ConditionalOnMissingBean
	public RetryPolicy exponentialBackoffRetry() {
		return new ExponentialBackoffRetry(zookeeperProperties.getBaseSleepTimeMs(), zookeeperProperties.getMaxRetries(), zookeeperProperties.getMaxSleepMs());
	}
}
