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
package io.mandrel.endpoints.contracts.coordinator;

import io.mandrel.cluster.node.Node;
import io.mandrel.common.sync.Container;
import io.mandrel.endpoints.contracts.Contract;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface NodesContract extends Contract, AutoCloseable {

	@ThriftMethod
	Map<String, Node> getNodes();

	@ThriftMethod
	Map<String, Node> getNodesByIds(Collection<String> ids);

	@ThriftMethod
	Node getNode(String id);

	@ThriftMethod
	Map<String, List<Container>> getContainers(String id);
}
