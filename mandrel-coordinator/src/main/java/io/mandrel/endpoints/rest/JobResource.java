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
package io.mandrel.endpoints.rest;

import io.mandrel.common.data.Job;
import io.mandrel.job.JobService;
import io.mandrel.metrics.JobMetrics;
import io.mandrel.metrics.MetricsService;
import io.mandrel.transport.Clients;

import java.util.List;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "/jobs")
@RequestMapping(value = Apis.PREFIX + "/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JobResource {

	private final Clients clients;
	private final JobService jobService;
	private final MetricsService metricsService;

	@ApiOperation(value = "List all the jobs", httpMethod = "GET", response = Job.class, responseContainer = "List")
	@RequestMapping(method = RequestMethod.GET)
	public Page<Job> all(@PageableDefault(page = 0, size = 20) Pageable pageable) {
		return jobService.page(pageable);
	}

	@ApiOperation(value = "Add a job", httpMethod = "GET")
	@RequestMapping(method = RequestMethod.GET, params = "urls")
	public Job add(@RequestParam List<String> urls) throws BindException {
		return jobService.add(urls);
	}

	@ApiOperation(value = "Add a job", httpMethod = "POST")
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Job add(@RequestBody Job job) throws BindException {
		return jobService.add(job);
	}

	@ApiOperation(value = "Update a job", httpMethod = "PUT", response = Job.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Job update(@PathVariable Long id, @RequestBody Job job) throws BindException {
		return jobService.update(job);
	}

	@ApiOperation(value = "Find a job by its id", httpMethod = "GET", response = Job.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Job id(@PathVariable Long id) {
		return jobService.get(id);
	}

	@ApiOperation(value = "Start a job", httpMethod = "GET")
	@RequestMapping(value = "/{id}/start", method = RequestMethod.GET)
	public void start(@PathVariable Long id) {
		jobService.start(id);
	}

	@ApiOperation(value = "Analyze a source against a job", httpMethod = "GET")
	@RequestMapping(value = "/{id}/analyze", method = RequestMethod.GET)
	public byte[] analyze(@PathVariable Long id, @RequestParam String source) {
		return clients.onRandomWorker().map(w -> w.analyse(id, source));
	}

	@ApiOperation(value = "Pause a job", httpMethod = "GET")
	@RequestMapping(value = "/{id}/pause", method = RequestMethod.GET)
	public void pause(@PathVariable Long id) {
		jobService.pause(id);
	}

	@ApiOperation(value = "Cancel a job", httpMethod = "GET")
	@RequestMapping(value = "/{id}/cancel", method = RequestMethod.GET)
	public void cancel(@PathVariable Long id) {
		jobService.kill(id);
	}

	@ApiOperation(value = "Delete a job", httpMethod = "DELETE")
	@RequestMapping(value = "/{id}/delete", method = RequestMethod.DELETE)
	public void delete(@PathVariable Long id) {
		jobService.delete(id);
	}

	@ApiOperation(value = "Retrieve the stats of a job", httpMethod = "GET")
	@RequestMapping(value = "/{id}/stats", method = RequestMethod.GET)
	public JobMetrics stats(@PathVariable Long id) {
		Job job = jobService.get(id);
		return metricsService.job(job.getId());
	}
}
