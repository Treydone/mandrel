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
package io.mandrel.requests.dns;

import io.mandrel.common.service.TaskContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class InternalNameResolver extends NameResolver {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class InternalNameResolverDefinition extends NameResolverDefinition<InternalNameResolver> {
		private static final long serialVersionUID = -2800579764535044200L;

		@Override
		public InternalNameResolver build(TaskContext context) {
			return new InternalNameResolver(context);
		}

		@Override
		public String name() {
			return "simple";
		}
	}

	public InternalNameResolver(TaskContext context) {
		super(context);
	}

	public InetAddress resolve(String name) throws UnknownHostException {
		return InetAddress.getByName(name);
	}

	@Override
	public void init() {
	}
}
