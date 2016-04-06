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

import io.mandrel.cluster.discovery.Service;
import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.common.NotFoundException;
import io.mandrel.common.container.ContainerStatus;
import io.mandrel.common.data.Job;
import io.mandrel.common.data.JobStatuses;
import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.coordinator.CoordinatorContainer;
import io.mandrel.coordinator.CoordinatorContainers;
import io.mandrel.endpoints.contracts.coordinator.AdminCoordinatorContract;
import io.mandrel.transport.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.weakref.jmx.internal.guava.base.Throwables;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CoordinatorResource implements AdminCoordinatorContract, Service {

	private final ObjectMapper objectMapper;

	private Supplier<? extends NotFoundException> coordinatorNotFound = () -> new NotFoundException("Coordinator not found");

	public String getServiceName() {
		return ServiceIds.coordinator();
	}

	@Override
	public void createCoordinatorContainer(byte[] definition) {
		Job job;
		try {
			job = objectMapper.readValue(definition, Job.class);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		create(job);
	}

	public void create(Job job) {
		CoordinatorContainer container = new CoordinatorContainer(job, null, null);
		container.register();
	}

	@Override
	public void startCoordinatorContainer(Long id) {
		CoordinatorContainers.get(id).orElseThrow(coordinatorNotFound).start();
	}

	@Override
	public void pauseCoordinatorContainer(Long id) {
		CoordinatorContainers.get(id).orElseThrow(coordinatorNotFound).pause();
	}

	@Override
	public void killCoordinatorContainer(Long id) {
		CoordinatorContainer container = CoordinatorContainers.get(id).orElseThrow(coordinatorNotFound);
		container.kill();
		container.unregister();
	}

	@Override
	public List<Container> listRunningCoordinatorContainers() {
		return CoordinatorContainers.list().stream().map(f -> new Container().setJobId(f.job().getId()).setVersion(f.job().getVersion()).setStatus(f.status()))
				.collect(Collectors.toList());
	}

	@Override
	public SyncResponse syncCoordinators(SyncRequest sync) throws RemoteException {
		Collection<? extends io.mandrel.common.container.Container> containers = CoordinatorContainers.list();
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
		containers.forEach(c -> {

			Job containerJob = c.job();
			long containerJobId = containerJob.getId();

			existingJobs.add(containerJobId);
			if (!jobByIdFromCoordinator.containsKey(containerJobId)) {
				log.debug("Killing job {}", containerJobId);
				killCoordinatorContainer(containerJobId);
				killed.add(containerJobId);
			} else {
				Job remoteJob = jobByIdFromCoordinator.get(containerJobId);

				if (remoteJob.getVersion() != containerJob.getVersion()) {
					log.debug("Updating job {}", containerJobId);
					killCoordinatorContainer(containerJobId);
					create(remoteJob);
					updated.add(containerJobId);

					if (JobStatuses.STARTED.equals(remoteJob.getStatus())) {
						log.debug("Starting job {}", containerJobId);
						startCoordinatorContainer(containerJobId);
						started.add(containerJobId);
					}
				} else if (!remoteJob.getStatus().toString().equalsIgnoreCase(c.status().toString())) {
					log.info("Container for {} is {}, but has to be {}", containerJob.getId(), c.status(), remoteJob.getStatus());

					switch (remoteJob.getStatus().getStatus()) {
					case STARTED:
						if (!ContainerStatus.STARTED.equals(c.status())) {
							log.debug("Starting job {}", containerJobId);
							startCoordinatorContainer(containerJobId);
							started.add(containerJobId);
						}
						break;
					case INITIATED:
						if (!ContainerStatus.INITIATED.equals(c.status())) {
							log.debug("Re-init job {}", containerJobId);
							killCoordinatorContainer(containerJobId);
							create(remoteJob);
						}
						break;
					case PAUSED:
						if (!ContainerStatus.PAUSED.equals(c.status())) {
							log.debug("Pausing job {}", containerJobId);
							pauseCoordinatorContainer(containerJobId);
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
				log.debug("Creating job {}...", id);
				create(job);
				created.add(job.getId());

				if (JobStatuses.STARTED.equals(job.getStatus())) {
					log.debug("Starting job {}", id);
					startCoordinatorContainer(job.getId());
					started.add(job.getId());
				} else if (JobStatuses.PAUSED.equals(job.getStatus())) {
					log.debug("Pausing job {}", id);
					pauseCoordinatorContainer(job.getId());
					paused.add(job.getId());
				}
			}
		});
		return response;
	}

	@Override
	public void close() throws Exception {

	}
}
