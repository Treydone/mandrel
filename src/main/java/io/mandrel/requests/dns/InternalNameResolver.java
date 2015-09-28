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

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.Data;

@Data
public class InternalNameResolver implements NameResolver {

	private static final long serialVersionUID = -7534644889369417852L;

	public InetAddress resolve(String name) throws UnknownHostException {
		return InetAddress.getByName(name);
	}

	public void init() {
	}

	@Override
	public String getType() {
		return "simple";
	}
}
