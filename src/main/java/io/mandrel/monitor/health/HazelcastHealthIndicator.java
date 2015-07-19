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
package io.mandrel.monitor.health;

import javax.inject.Inject;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
public class HazelcastHealthIndicator extends AbstractHealthIndicator {

	private final HazelcastInstance hazelcastInstance;

	@Inject
	public HazelcastHealthIndicator(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		try {
			hazelcastInstance.getCluster();
			builder.up();
		} catch (Exception e) {
			builder.down(e);
		}
	}
}
