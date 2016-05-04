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
package io.mandrel.endpoints.internal;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.Service;
import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.common.NotFoundException;
import io.mandrel.common.container.ContainerStatus;
import io.mandrel.common.data.Job;
import io.mandrel.common.data.JobStatuses;
import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.data.analysis.AnalysisService;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.endpoints.contracts.worker.AdminWorkerContract;
import io.mandrel.endpoints.contracts.worker.WorkerContract;
import io.mandrel.metrics.Accumulators;
import io.mandrel.transport.MandrelClient;
import io.mandrel.worker.WorkerContainer;
import io.mandrel.worker.WorkerContainers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.weakref.jmx.internal.guava.base.Throwables;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WorkerResource implements WorkerContract, AdminWorkerContract, Service {

	private final MandrelClient client;
	private final DiscoveryClient discoveryClient;
	private final ObjectMapper objectMapper;
	private final AnalysisService analysisService;
	private final ExtractorService extractorService;
	private final Accumulators accumulators;

	private Supplier<? extends NotFoundException> workerNotFound = () -> new NotFoundException("Worker not found");

	public String getServiceName() {
		return ServiceIds.worker();
	}

	@Override
	public void createWorkerContainer(byte[] definition) {
		Job job;
		try {
			job = objectMapper.readValue(definition, Job.class);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		create(job);
	}

	public void create(Job job) {
		WorkerContainer container = new WorkerContainer(extractorService, accumulators, job, client, discoveryClient);
		container.register();
	}

	@Override
	public void startWorkerContainer(Long id) {
		WorkerContainers.get(id).orElseThrow(workerNotFound).start();
	}

	@Override
	public void pauseWorkerContainer(Long id) {
		WorkerContainers.get(id).orElseThrow(workerNotFound).pause();
	}

	@Override
	public void killWorkerContainer(Long id) {
		WorkerContainer container = WorkerContainers.get(id).orElseThrow(workerNotFound);
		container.kill();
		container.unregister();
	}

	@Override
	public List<Container> listRunningWorkerContainers() {
		return WorkerContainers.list().stream().map(f -> new Container().setJobId(f.job().getId()).setVersion(f.job().getVersion()).setStatus(f.status()))
				.collect(Collectors.toList());
	}

	@Override
	public SyncResponse syncWorkers(SyncRequest sync) {
		Collection<? extends io.mandrel.common.container.Container> containers = WorkerContainers.list();
		final Map<Long, Job> jobByIdFromCoordinator = new HashMap<>();
		if (sync.getDefinitions() != null) {
			jobByIdFromCoordinator.putAll(sync.getDefinitions().stream().map(def -> {
				try {
					return objectMapper.readValue(def, Job.class);
				} catch (Exception e) {
					throw Throwables.propagate(e);
				}
			}).collect(Collectors.toMap(job -> job.getId(), job -> job)));
		}

		SyncResponse response = new SyncResponse();
		List<Long> created = new ArrayList<>();
		List<Long> started = new ArrayList<>();
		List<Long> updated = new ArrayList<>();
		List<Long> killed = new ArrayList<>();
		List<Long> paused = new ArrayList<>();
		response.setCreated(created).setKilled(killed).setUpdated(updated).setStarted(started);

		List<Long> existingJobs = new ArrayList<>();
		if (containers != null)
			containers.forEach(c -> {

				Job containerJob = c.job();
				long containerJobId = containerJob.getId();

				existingJobs.add(containerJobId);
				if (!jobByIdFromCoordinator.containsKey(containerJobId)) {
					log.debug("Killing job {}", containerJobId);
					killWorkerContainer(containerJobId);
					killed.add(containerJobId);
				} else {
					Job remoteJob = jobByIdFromCoordinator.get(containerJobId);

					if (remoteJob.getVersion() != containerJob.getVersion()) {
						log.debug("Updating job {}", containerJobId);
						killWorkerContainer(containerJobId);
						create(remoteJob);
						updated.add(containerJobId);

						if (JobStatuses.STARTED.equals(remoteJob.getStatus())) {
							log.debug("Starting job {}", containerJobId);
							startWorkerContainer(containerJobId);
							started.add(containerJobId);
						}
					} else if (!remoteJob.getStatus().toString().equalsIgnoreCase(c.status().toString())) {
						log.info("Container for {} is {}, but has to be {}", containerJob.getId(), c.status(), remoteJob.getStatus());

						switch (remoteJob.getStatus().getStatus()) {
						case STARTED:
							if (!ContainerStatus.STARTED.equals(c.status())) {
								log.debug("Starting job {}", containerJobId);
								startWorkerContainer(containerJobId);
								started.add(containerJobId);
							}
							break;
						case INITIATED:
							if (!ContainerStatus.INITIATED.equals(c.status())) {
								log.debug("Re-create job {}", containerJobId);
								killWorkerContainer(containerJobId);
								create(remoteJob);
							}
							break;
						case PAUSED:
							if (!ContainerStatus.PAUSED.equals(c.status())) {
								log.debug("Pausing job {}", containerJobId);
								pauseWorkerContainer(containerJobId);
								paused.add(containerJobId);
							}
							break;
						default:
							break;
						}
					}
				}
			});

		jobByIdFromCoordinator.forEach((id, job) -> {
			if (!existingJobs.contains(id)) {
				log.debug("Creating job {}", id);
				create(job);
				created.add(job.getId());

				if (JobStatuses.STARTED.equals(job.getStatus())) {
					log.debug("Starting job {}", id);
					startWorkerContainer(job.getId());
					started.add(job.getId());
				} else if (JobStatuses.PAUSED.equals(job.getStatus())) {
					log.debug("Pausing job {}", id);
					pauseWorkerContainer(job.getId());
					paused.add(job.getId());
				}
			}
		});
		return response;
	}

	@Override
	@SneakyThrows
	public byte[] analyse(Long id, String source) {
		Job job = WorkerContainers.get(id).orElseThrow(workerNotFound).job();
		return objectMapper.writeValueAsBytes(analysisService.analyze(job, source));
	}

	@Override
	public void close() throws Exception {

	}
}
