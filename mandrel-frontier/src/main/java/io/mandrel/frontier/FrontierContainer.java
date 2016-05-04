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
package io.mandrel.frontier;

import io.mandrel.common.container.AbstractContainer;
import io.mandrel.common.container.ContainerStatus;
import io.mandrel.common.data.Job;
import io.mandrel.common.service.TaskContext;
import io.mandrel.data.source.Source;
import io.mandrel.metadata.MetadataStore;
import io.mandrel.metadata.MetadataStores;
import io.mandrel.metrics.Accumulators;
import io.mandrel.transport.MandrelClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class FrontierContainer extends AbstractContainer {

	private final Monitor monitor = new Monitor();
	private final TaskContext context = new TaskContext();
	private final ExecutorService executor;
	private final Frontier frontier;
	private final Revisitor revisitor;

	public FrontierContainer(Job job, Accumulators accumulators, MandrelClient client) {
		super(accumulators, job, client);
		context.setDefinition(job);

		// Init stores
		MetadataStore metadatastore = job.getDefinition().getStores().getMetadataStore().build(context);
		metadatastore.init();
		MetadataStores.add(job.getId(), metadatastore);

		// Init frontier
		frontier = job.getDefinition().getFrontier().build(context);

		// Revisitor
		BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("frontier-" + job.getId() + "-%d").daemon(true)
				.priority(Thread.MAX_PRIORITY).build();
		executor = Executors.newFixedThreadPool(1, threadFactory);
		revisitor = new Revisitor(frontier, metadatastore);
		executor.submit(revisitor);

		current.set(ContainerStatus.INITIATED);
	}

	@Override
	public String type() {
		return "frontier";
	}

	@Override
	public void start() {
		if (monitor.tryEnter()) {
			try {
				if (!current.get().equals(ContainerStatus.STARTED)) {

					log.debug("Starting the frontier");
					frontier.init();

					// Init sources
					log.debug("Initializing the sources");
					job.getDefinition().getSources().forEach(s -> {
						Source source = s.build(context);
						if (!source.singleton() && source.check()) {
							source.register(uri -> {
								frontier.schedule(uri);
							});
						}
					});

					revisitor.start();

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
					log.debug("Pausing the frontier");

					revisitor.pause();

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
					try {
						executor.shutdownNow();
					} catch (Exception e) {
						log.debug(e.getMessage(), e);
					}

					try {
						frontier.destroy();
					} catch (Exception e) {
						log.warn("Can not destroy the frontier");
					}

					try {
						accumulators.destroy(job.getId());
					} catch (Exception e) {
						log.warn("Can not destroy the accumulators");
					}

					current.set(ContainerStatus.KILLED);
				}
			} finally {
				monitor.leave();
			}
		}
	}

	@Override
	public void register() {
		FrontierContainers.add(job.getId(), this);
	}

	@Override
	public void unregister() {
		FrontierContainers.remove(job.getId());
	}
}
