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

import io.mandrel.common.NotFoundException;
import io.mandrel.common.container.ContainerStatus;
import io.mandrel.common.data.Job;
import io.mandrel.common.data.JobStatuses;
import io.mandrel.common.net.Uri;
import io.mandrel.common.sync.Container;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.endpoints.contracts.FrontierContract;
import io.mandrel.endpoints.contracts.Next;
import io.mandrel.frontier.FrontierContainer;
import io.mandrel.frontier.FrontierContainers;
import io.mandrel.metrics.Accumulators;
import io.mandrel.transport.Clients;
import io.mandrel.transport.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.weakref.jmx.internal.guava.base.Throwables;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

@Component
@Slf4j
public class FrontierResource implements FrontierContract {

	@Autowired
	private Accumulators accumulators;
	@Autowired
	private Clients clients;
	@Autowired
	private ObjectMapper objectMapper;

	private Supplier<? extends NotFoundException> frontierNotFound = () -> new NotFoundException("Frontier not found");

	@Override
	public void createFrontierContainer(byte[] definition) {
		Job job;
		try {
			job = objectMapper.readValue(definition, Job.class);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		create(job);
	}

	public void create(Job job) {
		FrontierContainer container = new FrontierContainer(job, accumulators, clients);
		container.register();
	}

	@Override
	public void startFrontierContainer(Long id) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).start();
	}

	@Override
	public void pauseFrontierContainer(Long id) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).pause();
	}

	@Override
	public void killFrontierContainer(Long id) {
		FrontierContainer container = FrontierContainers.get(id).orElseThrow(frontierNotFound);
		container.kill();
		container.unregister();
	}

	@Override
	public void schedule(Long id, Uri uri) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).frontier().schedule(uri);
	}

	@Override
	public void mschedule(Long id, Set<Uri> uris) {
		FrontierContainers.get(id).orElseThrow(frontierNotFound).frontier().schedule(uris);
	}

	@Override
	public List<Container> listRunningFrontierContainers() {
		return FrontierContainers.list().stream()
				.map(f -> new Container().setJobId(f.job().getId()).setVersion(f.job().getVersion()).setStatus(f.status()))
				.collect(Collectors.toList());
	}

	@Override
	public ListenableFuture<Next> next(Long id) {
		FrontierContainer frontier = FrontierContainers.get(id).orElseThrow(frontierNotFound);
		SettableFuture<Next> result = SettableFuture.create();
		frontier.frontier().pool((uri, name) -> {
			result.set(new Next().setUri(uri).setFromStore(name));
		});
		try {
			return result;
		} catch (Exception e) {
			log.debug("Well...", e);
			throw RemoteException.of(RemoteException.Error.G_UNKNOWN, e.getMessage());
		}
	}

	@Override
	public SyncResponse syncFrontiers(SyncRequest sync) {
		Collection<? extends io.mandrel.common.container.Container> containers = FrontierContainers.list();
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
					killFrontierContainer(containerJobId);
					killed.add(containerJobId);
				} else {
					Job remoteJob = jobByIdFromCoordinator.get(containerJobId);

					if (remoteJob.getVersion() != containerJob.getVersion()) {
						log.debug("Updating job {}", containerJobId);
						killFrontierContainer(containerJobId);
						create(remoteJob);
						updated.add(containerJobId);

						if (JobStatuses.STARTED.equals(remoteJob.getStatus())) {
							log.debug("Starting job {}", containerJobId);
							startFrontierContainer(containerJobId);
							started.add(containerJobId);
						}
					} else if (!remoteJob.getStatus().equalsIgnoreCase(c.status().toString())) {
						log.info("Container for {} is {}, but has to be {}", containerJob.getId(), c.status(), remoteJob.getStatus());

						switch (remoteJob.getStatus()) {
						case JobStatuses.STARTED:
							if (!ContainerStatus.STARTED.equals(c.status())) {
								log.debug("Starting job {}", containerJobId);
								startFrontierContainer(containerJobId);
								started.add(containerJobId);
							}
							break;
						case JobStatuses.INITIATED:
							if (!ContainerStatus.INITIATED.equals(c.status())) {
								log.debug("Re-init job {}", containerJobId);
								killFrontierContainer(containerJobId);
								create(remoteJob);
							}
							break;
						case JobStatuses.PAUSED:
							if (!ContainerStatus.PAUSED.equals(c.status())) {
								log.debug("Pausing job {}", containerJobId);
								pauseFrontierContainer(containerJobId);
								paused.add(containerJobId);
							}
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
					startFrontierContainer(job.getId());
					started.add(job.getId());
				} else if (JobStatuses.PAUSED.equals(job.getStatus())) {
					log.debug("Pausing job {}", id);
					pauseFrontierContainer(job.getId());
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
