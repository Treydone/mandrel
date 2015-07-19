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
package io.mandrel.cluster.idgenerator.hazelcast;

import io.mandrel.cluster.idgenerator.IdGenerator;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
public class HazelcastIdGenerator implements IdGenerator {

	private final HazelcastInstance instance;

	@Override
	public long generateId(String name) {
		return instance.getIdGenerator(name).newId();
	}
}
