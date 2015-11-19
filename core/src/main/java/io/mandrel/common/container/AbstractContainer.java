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
package io.mandrel.common.container;

import io.mandrel.common.client.Clients;
import io.mandrel.common.data.Spider;
import io.mandrel.metrics.Accumulators;

import java.util.concurrent.atomic.AtomicReference;

import lombok.Data;
import lombok.experimental.Accessors;

import org.springframework.cloud.client.discovery.DiscoveryClient;

@Data
@Accessors(chain = true, fluent = true)
public abstract class AbstractContainer implements Container {

	protected final Accumulators accumulators;
	protected final Spider spider;
	protected final Clients clients;
	protected final DiscoveryClient discoveryClient;

	protected final AtomicReference<Status> current = new AtomicReference<>(Status.CREATED);

	@Override
	public Status status() {
		return current.get();
	}
}
