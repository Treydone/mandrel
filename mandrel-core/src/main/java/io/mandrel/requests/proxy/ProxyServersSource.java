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
package io.mandrel.requests.proxy;

import io.mandrel.common.data.Spider;
import io.mandrel.common.lifecycle.Initializable;
import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.service.ObjectFactory;
import io.mandrel.common.service.TaskContext;
import io.mandrel.common.service.TaskContextAware;

import java.io.Serializable;

public abstract class ProxyServersSource extends TaskContextAware implements Initializable {

	public ProxyServersSource(TaskContext context) {
		super(context);
	}

	public static abstract class ProxyServersSourceDefinition<PROXYSERVERSSOURCE extends ProxyServersSource> implements NamedDefinition,
			ObjectFactory<PROXYSERVERSSOURCE>, Serializable {
		private static final long serialVersionUID = 4221420676233532250L;

	}

	public abstract ProxyServer findProxy(Spider spider);
}
