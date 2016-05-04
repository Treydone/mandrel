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
package io.mandrel.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Component
public class Accumulators {

	private final GlobalAccumulator global = new GlobalAccumulator();
	private final NodeAccumulator node = new NodeAccumulator();
	private final ConcurrentMap<Long, JobAccumulator> jobs = new ConcurrentHashMap<>();

	public GlobalAccumulator globalAccumulator() {
		return global;
	}

	public NodeAccumulator nodeAccumulator() {
		return node;
	}

	public Map<Long, JobAccumulator> jobAccumulators() {
		return jobs;
	}

	public JobAccumulator jobAccumulator(long jobId) {
		return jobs.computeIfAbsent(jobId, id -> new JobAccumulator(id));
	}

	public void destroy(long jobId) {
		jobs.remove(jobId);
	}
}
