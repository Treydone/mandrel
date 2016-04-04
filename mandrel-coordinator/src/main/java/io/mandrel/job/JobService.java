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

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.cluster.discovery.ServiceInstance;
import io.mandrel.cluster.instance.StateService;
import io.mandrel.common.MandrelException;
import io.mandrel.common.NotFoundException;
import io.mandrel.common.data.Job;
import io.mandrel.common.data.JobStatuses;
import io.mandrel.common.service.TaskContext;
import io.mandrel.common.sync.SyncRequest;
import io.mandrel.common.sync.SyncResponse;
import io.mandrel.data.filters.link.AllowedForDomainsFilter;
import io.mandrel.data.filters.link.SkipAncorFilter;
import io.mandrel.data.filters.link.UrlPatternFilter;
import io.mandrel.data.source.FixedSource.FixedSourceDefinition;
import io.mandrel.data.source.Source;
import io.mandrel.data.validation.Validators;
import io.mandrel.metrics.Accumulators;
import io.mandrel.metrics.MetricsService;
import io.mandrel.timeline.Event;
import io.mandrel.timeline.Event.JobInfo.JobEventType;
import io.mandrel.timeline.TimelineService;
import io.mandrel.transport.Clients;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.kohsuke.randname.RandomNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Monitor;

@Component
@Slf4j
public class JobService {

	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private TimelineService timelineService;
	@Autowired
	private Clients clients;
	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private StateService stateService;
	@Autowired
	private Accumulators accumulators;
	@Autowired
	private MetricsService metricsService;
	@Autowired
	private ObjectMapper objectMapper;

	private final RandomNameGenerator generator = new RandomNameGenerator();
	private final Monitor monitor = new Monitor();

	@PostConstruct
	public void init() {
		// TODO Load the journal of commands
	}

	@Scheduled(fixedRate = 2000)
	public void sync() {
		if (stateService.isStarted()) {
			// TODO HOW TO in case of multiple coordinator
			// -> Acquiring distributed lock
			if (monitor.tryEnter()) {
				try {
					log.trace("Syncing the nodes from the coordinator...");

					SyncRequest sync = new SyncRequest();

					// Load the existing jobs from the database
					List<Job> jobs = jobRepository.listActive();
					if (CollectionUtils.isNotEmpty(jobs)) {
						sync.setDefinitions(jobs.stream().map(job -> {
							try {
								return objectMapper.writeValueAsBytes(job);
							} catch (Exception e) {
								throw Throwables.propagate(e);
							}
						}).collect(Collectors.toList()));
					}
					// Sync first the coordinators
					discoveryClient.getInstances(ServiceIds.coordinator())
							.forEach(
									instance -> {
										log.trace("Syncing coordinator {}", instance);
										try {
											SyncResponse response = clients.onCoordinator(instance.getHostAndPort()).map(
													coordinator -> coordinator.syncCoordinators(sync));

											if (response.anyAction()) {
												log.debug("On coordinator {}:{}, after sync: {} created, {} updated, {} killed, {} started, {} paused",
														instance.getHost(), instance.getPort(), response.getCreated(), response.getUpdated(),
														response.getKilled(), response.getPaused());
											}
										} catch (Exception e) {
											log.warn("Can not sync coordinator {}:{} due to: {}", instance.getHost(), instance.getPort(), e.getMessage());
										}
									});

					// And then the frontiers
					discoveryClient.getInstances(ServiceIds.frontier()).forEach(
							instance -> {
								log.trace("Syncing frontier {}", instance);
								try {
									SyncResponse response = clients.onFrontier(instance.getHostAndPort()).map(frontier -> frontier.syncFrontiers(sync));

									if (response.anyAction()) {
										log.debug("On frontier {}:{}, after sync: {} created, {} updated, {} killed, {} started, {} paused",
												instance.getHost(), instance.getPort(), response.getCreated(), response.getUpdated(), response.getKilled(),
												response.getPaused());
									}
								} catch (Exception e) {
									log.warn("Can not sync frontier {}:{} due to: {}", instance.getHost(), instance.getPort(), e.getMessage());
								}
							});

					// And then the workers
					discoveryClient.getInstances(ServiceIds.worker()).forEach(
							instance -> {
								log.trace("Syncing worker {}", instance);
								try {
									SyncResponse response = clients.onWorker(instance.getHostAndPort()).map(worker -> worker.syncWorkers(sync));

									if (response.anyAction()) {
										log.debug("On frontier {}:{}, after sync: {} created, {} updated, {} killed, {} started, {} paused",
												instance.getHost(), instance.getPort(), response.getCreated(), response.getUpdated(), response.getKilled(),
												response.getPaused());
									}
								} catch (Exception e) {
									log.warn("Can not sync worker {}:{} due to: {}", instance.getHost(), instance.getPort(), e.getMessage());
								}
							});

				} finally {
					monitor.leave();
				}
			}
		}
	}

	public Job update(Job job) throws BindException {
		BindingResult errors = Validators.validate(job);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			throw new BindException(errors);
		}

		updateTimeline(job, JobEventType.SPIDER_UPDATED);

		return jobRepository.update(job);
	}

	public void updateTimeline(Job job, JobEventType status) {
		Event event = Event.forJob();
		event.getJob().setJobId(job.getId()).setJobName(job.getName()).setType(status);
		timelineService.add(event);
	}

	/**
	 * Create a new job from a fixed list of urls.
	 * 
	 * @param urls
	 * @return
	 */
	public Job add(List<String> urls) throws BindException {
		Job job = new Job();
		job.setName(generator.next());

		// Add source
		FixedSourceDefinition source = new FixedSourceDefinition();
		source.setUrls(urls);
		job.setSources(Arrays.asList(source));

		// Add filters
		job.getFilters().getLinks().add(new AllowedForDomainsFilter().domains(urls.stream().map(url -> {
			return URI.create(url).getHost();
		}).collect(Collectors.toList())));
		job.getFilters().getLinks().add(new SkipAncorFilter());
		job.getFilters().getLinks().add(UrlPatternFilter.STATIC);

		return add(job);
	}

	public long fork(long id) throws BindException {
		Job job = get(id);
		job.setId(0);
		job.setName(generator.next());

		cleanDates(job);

		BindingResult errors = Validators.validate(job);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			throw new BindException(errors);
		}

		job.setStatus(JobStatuses.INITIATED);
		job.setCreated(LocalDateTime.now());

		job = jobRepository.add(job);

		return job.getId();
	}

	public void cleanDates(Job job) {
		job.setCreated(null);
		job.setDeleted(null);
		job.setEnded(null);
		job.setKilled(null);
		job.setPaused(null);
		job.setStarted(null);
	}

	public Job add(Job job) throws BindException {
		BindingResult errors = Validators.validate(job);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			throw new BindException(errors);
		}

		job.setStatus(JobStatuses.INITIATED);
		job.setCreated(LocalDateTime.now());
		job = jobRepository.add(job);

		updateTimeline(job, JobEventType.SPIDER_CREATED);

		return job;
	}

	public Job get(long id) {
		return jobRepository.get(id).orElseThrow(() -> new NotFoundException("Job not found"));
	}

	public Page<Job> page(Pageable pageable) {
		return jobRepository.page(pageable);
	}

	public Page<Job> pageForActive(Pageable pageable) {
		return jobRepository.pageForActive(pageable);
	}

	public List<Job> listLastActive(int limit) {
		return jobRepository.listLastActive(limit);
	}

	public void reinject(long jobId) {
		Job job = get(jobId);
		injectSingletonSources(job);
	}

	public void start(long jobId) {
		Job job = get(jobId);

		if (JobStatuses.STARTED.equals(job.getStatus())) {
			return;
		}

		if (JobStatuses.KILLED.equals(job.getStatus())) {
			throw new MandrelException("Job cancelled!");
		}

		// Can not start a job if there no frontier started
		if (discoveryClient.getInstances(ServiceIds.frontier()).size() < 1) {
			throw new MandrelException("Can not start job, you need a least a frontier instance!");
		}

		if (JobStatuses.INITIATED.equals(job.getStatus())) {
			// injectSingletonSources(job);
		}

		jobRepository.updateStatus(jobId, JobStatuses.STARTED);

		updateTimeline(job, JobEventType.SPIDER_CREATED);

	}

	public void injectSingletonSources(Job job) {
		// Deploy singleton sources on a random frontier
		TaskContext context = new TaskContext();
		context.setDefinition(job);

		job.getSources().forEach(s -> {
			Source source = s.build(context);

			if (source.singleton() && source.check()) {
				log.debug("Injecting source '{}' ({})", s.name(), s.toString());
				ServiceInstance instance = discoveryClient.getInstances(ServiceIds.frontier()).get(0);

				source.register(uri -> {
					try {
						log.trace("Adding uri '{}'", uri);
						clients.onFrontier(instance.getHostAndPort()).with(frontier -> frontier.schedule(job.getId(), uri));
					} catch (Exception e) {
						log.warn("Can not sync due to", e);
					}
				});
			}
		});
	}

	public void pause(long jobId) {
		Job job = get(jobId);

		// Update status
		jobRepository.updateStatus(jobId, JobStatuses.PAUSED);

		updateTimeline(job, JobEventType.SPIDER_PAUSED);

	}

	public void kill(long jobId) {
		Job job = get(jobId);

		// Update status
		jobRepository.updateStatus(jobId, JobStatuses.KILLED);

		updateTimeline(job, JobEventType.SPIDER_KILLED);
	}

	public void delete(long jobId) {
		Job job = get(jobId);

		// Update status
		jobRepository.updateStatus(jobId, JobStatuses.DELETED);

		updateTimeline(job, JobEventType.SPIDER_DELETED);

		metricsService.delete(jobId);

	}
}
