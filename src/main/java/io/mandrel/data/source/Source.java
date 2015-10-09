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
package io.mandrel.data.source;

import io.mandrel.common.loader.NamedComponent;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;

@Data
@Accessors(chain = true)
public abstract class Source implements NamedComponent, Serializable {

	private static final long serialVersionUID = 7468260753688101634L;

	@Getter(onMethod = @__(@JsonIgnore))
	private transient HazelcastInstance instance;

	public abstract void register(EntryListener listener);

	abstract public boolean check();

	public boolean singleton() {
		return true;
	}

	public void init(Map<String, Object> properties) {

	}
}
