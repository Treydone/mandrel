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
package io.mandrel.frontier;

import java.net.URI;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SimpleFrontier extends Frontier {

	private static final long serialVersionUID = -4055424223863734294L;

	private static final String DEFAULT_QUEUE = "default";

	@Override
	public void create() {
		getStore().create(DEFAULT_QUEUE);
	}

	@Override
	public URI pool() {
		return getStore().queue(DEFAULT_QUEUE).pool();
	}

	@Override
	public void schedule(URI uri) {
		getStore().queue(DEFAULT_QUEUE).schedule(uri);
	}

	@Override
	public void finished(URI uri) {
		getStore().finish(uri);
	}

	@Override
	public void delete(URI uri) {
		getStore().delete(uri);
	}

	@Override
	public String name() {
		return "simple";
	}
}
