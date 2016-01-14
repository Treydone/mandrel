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

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("discovery.zookeeper")
@Data
public class ZookeeperDiscoveryProperties {

	private boolean enabled = true;

	private String root = "/mandrel";

	private String uriSpec = "{scheme}://{address}:{port}";

	@NotNull
	private String connectString = "localhost:2181";

	/**
	 * @param baseSleepTimeMs
	 *            initial amount of time to wait between retries
	 */
	private Integer baseSleepTimeMs = 50;

	/**
	 * @param maxRetries
	 *            max number of times to retry
	 */
	private Integer maxRetries = 10;

	/**
	 * @param maxSleepMs
	 *            max time in ms to sleep on each retry
	 */
	private Integer maxSleepMs = 500;

	private Integer blockUntilConnectedWait = 10;

	private TimeUnit blockUntilConnectedUnit = TimeUnit.SECONDS;
}
