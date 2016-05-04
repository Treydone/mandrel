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

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.endpoints.contracts.NodeContract;
import io.mandrel.endpoints.contracts.coordinator.AdminCoordinatorContract;
import io.mandrel.endpoints.contracts.coordinator.JobsContract;
import io.mandrel.endpoints.contracts.coordinator.MetricsContract;
import io.mandrel.endpoints.contracts.coordinator.NodesContract;
import io.mandrel.endpoints.contracts.coordinator.TimelineContract;
import io.mandrel.endpoints.contracts.frontier.AdminFrontierContract;
import io.mandrel.endpoints.contracts.frontier.FrontierContract;
import io.mandrel.endpoints.contracts.worker.AdminWorkerContract;
import io.mandrel.endpoints.contracts.worker.WorkerContract;

public interface MandrelClient {

	DiscoveryClient discovery();

	FrontierClient frontier();

	CoordinatorClient coordinator();

	WorkerClient worker();

	Targeted<NodeContract> node();

	public interface FrontierClient extends Client {

		Targeted<FrontierContract> client();

		Targeted<AdminFrontierContract> admin();
	}

	public interface WorkerClient extends Client {

		Targeted<AdminWorkerContract> admin();

		Targeted<WorkerContract> client();
	}

	public interface CoordinatorClient extends Client {

		Targeted<AdminCoordinatorContract> admin();

		Targeted<TimelineContract> events();

		Targeted<JobsContract> jobs();

		Targeted<MetricsContract> metrics();

		Targeted<NodesContract> nodes();
	}

	public interface Client {

	}
}
