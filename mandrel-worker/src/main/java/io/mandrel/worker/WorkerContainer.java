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
package io.mandrel.worker;

import io.mandrel.blob.BlobStore;
import io.mandrel.blob.BlobStores;
import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.common.container.AbstractContainer;
import io.mandrel.common.container.ContainerStatus;
import io.mandrel.common.data.Job;
import io.mandrel.common.service.TaskContext;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.document.DocumentStore;
import io.mandrel.document.DocumentStores;
import io.mandrel.metadata.MetadataStore;
import io.mandrel.metadata.MetadataStores;
import io.mandrel.metrics.Accumulators;
import io.mandrel.requests.Requester;
import io.mandrel.requests.Requesters;
import io.mandrel.transport.Clients;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import com.google.common.util.concurrent.Monitor;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true, fluent = true)
public class WorkerContainer extends AbstractContainer {

	private final Monitor monitor = new Monitor();
	private final TaskContext context = new TaskContext();
	private final ExtractorService extractorService;
	private final ScheduledExecutorService executor;
	private final List<Loop> loops;

	public WorkerContainer(ExtractorService extractorService, Accumulators accumulators, Job job, Clients clients, DiscoveryClient discoveryClient) {
		super(accumulators, job, clients);
		context.setDefinition(job);

		this.extractorService = extractorService;

		// Create the thread factory
		BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("worker-" + job.getId() + "-%d").daemon(true)
				.priority(Thread.MAX_PRIORITY).build();

		// Get number of parallel loops
		int parallel = Runtime.getRuntime().availableProcessors();
		// Prepare a pool for the X parallel loops and the barrier refresh
		executor = Executors.newScheduledThreadPool(parallel + 1, threadFactory);

		// Prepare the barrier
		Barrier barrier = new Barrier(job.getPoliteness(), discoveryClient);
		executor.scheduleAtFixedRate(() -> barrier.updateBuckets(), 10, 10, TimeUnit.SECONDS);

		// Create loop
		loops = new ArrayList<>(parallel);
		IntStream.range(0, parallel).forEach(idx -> {
			Loop loop = new Loop(extractorService, job, clients, accumulators.jobAccumulator(job.getId()), accumulators.globalAccumulator(), barrier);
			loops.add(loop);
			executor.submit(loop);
		});

		// Init stores
		MetadataStore metadatastore = job.getStores().getMetadataStore().build(context);
		metadatastore.init();
		MetadataStores.add(job.getId(), metadatastore);

		BlobStore blobStore = job.getStores().getBlobStore().build(context);
		blobStore.init();
		BlobStores.add(job.getId(), blobStore);

		if (job.getExtractors().getData() != null) {
			job.getExtractors().getData().forEach(ex -> {
				DocumentStore documentStore = ex.getDocumentStore().metadataExtractor(ex).build(context);
				documentStore.init();
				DocumentStores.add(job.getId(), ex.getName(), documentStore);
			});
		}

		// Init requesters
		job.getClient().getRequesters().forEach(r -> {
			Requester requester = r.build(context);

			// Prepare client
				if (requester.nameResolver() != null) {
					requester.nameResolver().init();
				}
				if (requester.proxyServersSource() != null) {
					requester.proxyServersSource().init();
				}
				requester.init();

				Requesters.add(job.getId(), requester);
			});

		current.set(ContainerStatus.INITIATED);
	}

	@Override
	public String type() {
		return "worker";
	}

	@Override
	public void start() {
		if (monitor.tryEnter()) {
			try {
				if (!current.get().equals(ContainerStatus.STARTED)) {
					loops.forEach(loop -> loop.start());
					current.set(ContainerStatus.STARTED);
				}
			} finally {
				monitor.leave();
			}
		}
	}

	@Override
	public void pause() {
		if (monitor.tryEnter()) {
			try {
				if (!current.get().equals(ContainerStatus.PAUSED)) {
					loops.forEach(loop -> loop.pause());
					current.set(ContainerStatus.PAUSED);
				}
			} finally {
				monitor.leave();
			}
		}
	}

	@Override
	public void kill() {
		if (monitor.tryEnter()) {
			try {
				if (!current.get().equals(ContainerStatus.KILLED)) {
					loops.forEach(loop -> loop.stop());

					try {
						executor.shutdown(); // Disable new tasks from being submitted
						try {
							// Wait a while for existing tasks to terminate
							if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
								executor.shutdownNow(); // Cancel currently executing tasks
								// Wait a while for tasks to respond to being cancelled
								if (!executor.awaitTermination(60, TimeUnit.SECONDS))
									log.warn("Pool did not terminate");
							}
						} catch (InterruptedException ie) {
							// (Re-)Cancel if current thread also interrupted
							executor.shutdownNow();
							// Preserve interrupt status
							Thread.currentThread().interrupt();
						}
					} catch (Exception e) {
						log.warn(e.getMessage(), e);
					}

					try {
						MetadataStores.remove(job.getId());
					} catch (Exception e) {
						log.debug(e.getMessage(), e);
					}

					try {
						BlobStores.remove(job.getId());
					} catch (Exception e) {
						log.debug(e.getMessage(), e);
					}

					try {
						DocumentStores.remove(job.getId());
					} catch (Exception e) {
						log.debug(e.getMessage(), e);
					}

					try {
						Requesters.remove(job.getId());
					} catch (Exception e) {
						log.debug(e.getMessage(), e);
					}

					accumulators.destroy(job.getId());

					current.set(ContainerStatus.KILLED);
				}
			} finally {
				monitor.leave();
			}
		}
	}

	@Override
	public void register() {
		WorkerContainers.add(job.getId(), this);
	}

	@Override
	public void unregister() {
		WorkerContainers.remove(job.getId());
	}
}
