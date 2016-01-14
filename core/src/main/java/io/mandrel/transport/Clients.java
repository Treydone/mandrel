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
package io.mandrel.transport;

import io.mandrel.endpoints.contracts.ControllerContract;
import io.mandrel.endpoints.contracts.FrontierContract;
import io.mandrel.endpoints.contracts.NodeContract;
import io.mandrel.endpoints.contracts.WorkerContract;

import com.google.common.net.HostAndPort;

public interface Clients {

	Pooled<FrontierContract> onFrontier(HostAndPort hostAndPort);

	Pooled<FrontierContract> onRandomFrontier();

	Pooled<ControllerContract> onController(HostAndPort hostAndPort);

	Pooled<ControllerContract> onRandomController();

	Pooled<WorkerContract> onWorker(HostAndPort hostAndPort);

	Pooled<NodeContract> onNode(HostAndPort hostAndPort);
}
