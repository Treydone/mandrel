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
package io.mandrel.requests;

import io.mandrel.blob.Blob;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Strategy;
import io.mandrel.common.data.Strategy.StrategyDefinition;
import io.mandrel.common.lifecycle.Initializable;
import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.service.ObjectFactory;
import io.mandrel.common.service.TaskContext;
import io.mandrel.common.service.TaskContextAware;
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.net.URI;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true, fluent = true)
public abstract class Requester<STRATEGY extends Strategy> extends TaskContextAware implements Checkable, Initializable {

	public Requester(TaskContext context) {
		super(context);
	}

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static abstract class RequesterDefinition<STRATEGY extends Strategy, REQUESTER extends Requester<STRATEGY>> implements NamedDefinition,
			ObjectFactory<REQUESTER>, Serializable {
		private static final long serialVersionUID = 8753562363550894996L;

		@JsonProperty("strategy")
		private StrategyDefinition<STRATEGY> strategy;

		protected REQUESTER build(REQUESTER requester, TaskContext context) {
			requester.strategy(getStrategy().build(context));
			return requester;
		}
	}

	private STRATEGY strategy;

	public abstract Blob getBlocking(URI uri, Spider spider) throws Exception;

	public abstract Blob getBlocking(URI uri) throws Exception;

	public abstract Set<String> getProtocols();
}
