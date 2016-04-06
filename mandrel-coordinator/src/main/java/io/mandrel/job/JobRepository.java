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
package io.mandrel.job;

import io.mandrel.common.data.Job;
import io.mandrel.common.data.JobStatuses;
import io.mandrel.common.data.Page;
import io.mandrel.common.data.PageRequest;

import java.util.List;
import java.util.Optional;

public interface JobRepository {

	Job add(Job job);

	Job update(Job job);

	void updateStatus(long jobId, JobStatuses status);

	void delete(long id);

	Optional<Job> get(long id);

	List<Job> listActive();

	List<Job> listLastActive(int limit);

	Page<Job> page(PageRequest request);

	Page<Job> pageForActive(PageRequest request);

}
