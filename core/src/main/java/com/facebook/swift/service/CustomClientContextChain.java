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
package com.facebook.swift.service;

import java.util.List;

import com.facebook.nifty.client.ClientRequestContext;
import com.facebook.swift.service.ClientContextChain;
import com.facebook.swift.service.ThriftClientEventHandler;

public class CustomClientContextChain extends ClientContextChain {

	public CustomClientContextChain(List<? extends ThriftClientEventHandler> handlers, String methodName, ClientRequestContext requestContext) {
		super(handlers, methodName, requestContext);
	}
}
