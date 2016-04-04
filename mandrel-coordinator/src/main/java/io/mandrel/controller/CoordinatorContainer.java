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
package io.mandrel.controller;

import io.mandrel.blob.BlobStore;
import io.mandrel.blob.BlobStores;
import io.mandrel.common.container.AbstractContainer;
import io.mandrel.common.container.ContainerStatus;
import io.mandrel.common.data.Job;
import io.mandrel.common.service.TaskContext;
import io.mandrel.document.DocumentStore;
import io.mandrel.document.DocumentStores;
import io.mandrel.metadata.MetadataStore;
import io.mandrel.metadata.MetadataStores;
import io.mandrel.metrics.Accumulators;
import io.mandrel.transport.Clients;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import com.google.common.util.concurrent.Monitor;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true, fluent = true)
public class CoordinatorContainer extends AbstractContainer {

	private final Monitor monitor = new Monitor();
	private final TaskContext context = new TaskContext();

	public CoordinatorContainer(Job job, Accumulators accumulators, Clients clients) {
		super(accumulators, job, clients);
		context.setDefinition(job);

		// Init stores
		MetadataStore metadatastore = job.getStores().getMetadataStore().build(context);
		metadatastore.init();
		MetadataStores.add(job.getId(), metadatastore);

		BlobStore blobStore = job.getStores().getBlobStore().build(context);
		blobStore.init();
		BlobStores.add(job.getId(), blobStore);

		job.getExtractors().getData().forEach(ex -> {
			DocumentStore documentStore = ex.getDocumentStore().metadataExtractor(ex).build(context);
			documentStore.init();
			DocumentStores.add(job.getId(), ex.getName(), documentStore);
		});

		current.set(ContainerStatus.INITIATED);

	}

	@Override
	public String type() {
		return "coordinator";
	}

	@Override
	public void start() {
		if (monitor.tryEnter()) {
			try {
				if (!current.get().equals(ContainerStatus.STARTED)) {
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
					log.debug("Pausing the coordinator");
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
					
					current.set(ContainerStatus.KILLED);
				}
			} finally {
				monitor.leave();
			}
		}
	}

	@Override
	public void register() {
		CoordinatorContainers.add(job.getId(), this);
	}

	@Override
	public void unregister() {
		CoordinatorContainers.remove(job.getId());
	}
}
