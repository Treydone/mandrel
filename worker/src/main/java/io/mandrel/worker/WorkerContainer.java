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
import io.mandrel.common.container.AbstractContainer;
import io.mandrel.common.container.Status;
import io.mandrel.common.data.Spider;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true, fluent = true)
public class WorkerContainer extends AbstractContainer {

	private final ExtractorService extractorService;

	private ExecutorService executor;
	private List<Loop> loops;

	public WorkerContainer(ExtractorService extractorService, Accumulators accumulators, Spider spider, Clients clients) {
		super(accumulators, spider, clients);
		this.extractorService = extractorService;
		init();
	}

	public void init() {

		// Create the thread factory
		BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("workerthread-%d").daemon(true).priority(Thread.MAX_PRIORITY).build();

		// Get number of parallel loops
		int parallel = Runtime.getRuntime().availableProcessors();
		executor = Executors.newFixedThreadPool(parallel, threadFactory);

		// spider.getClient().

		// Create loop
		loops = new ArrayList<>(parallel);
		IntStream.range(0, parallel).forEach(idx -> {
			Loop loop = new Loop(extractorService, spider, clients, accumulators.spiderAccumulator(spider.getId()), accumulators.globalAccumulator());
			loops.add(loop);
			executor.submit(loop);
		});

		// Create context
		TaskContext context = new TaskContext();
		context.setDefinition(spider);

		// Init stores
		MetadataStore metadatastore = spider.getStores().getMetadataStore().build(context);
		metadatastore.init();
		MetadataStores.add(spider.getId(), metadatastore);

		BlobStore blobStore = spider.getStores().getBlobStore().build(context);
		blobStore.init();
		BlobStores.add(spider.getId(), blobStore);

		spider.getExtractors().getPages().forEach(ex -> {
			DocumentStore documentStore = ex.getDocumentStore().metadataExtractor(ex).build(context);
			documentStore.init();
			DocumentStores.add(spider.getId(), ex.getName(), documentStore);
		});

		// Init requesters
		spider.getClient().getRequesters().forEach(r -> {
			Requester<?> requester = r.build(context);

			// Prepare client
				if (requester.strategy().nameResolver() != null) {
					requester.strategy().nameResolver().init();
				}
				if (requester.strategy().proxyServersSource() != null) {
					requester.strategy().proxyServersSource().init();
				}
				requester.init();

				Requesters.add(spider.getId(), requester);
			});

		current.set(Status.INITIATED);
	}

	@Override
	public String type() {
		return "worker";
	}

	@Override
	public void start() {
		loops.forEach(loop -> loop.start());
		current.set(Status.STARTED);
	}

	@Override
	public void pause() {
		loops.forEach(loop -> loop.pause());
		current.set(Status.PAUSED);
	}

	@Override
	public void kill() {

		loops.forEach(loop -> loop.pause());
		try {
			executor.shutdownNow();
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}

		try {
			MetadataStores.remove(spider.getId());
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}

		try {
			BlobStores.remove(spider.getId());
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}

		try {
			DocumentStores.remove(spider.getId());
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}

		try {
			Requesters.remove(spider.getId());
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}

		accumulators.destroy(spider.getId());
	}

	@Override
	public void register() {
		WorkerContainer oldContainer = WorkerContainers.add(spider.getId(), this);
		if (oldContainer != null) {
			oldContainer.kill();
		}
	}

	@Override
	public void unregister() {
		WorkerContainer oldContainer = WorkerContainers.remove(spider.getId());
		if (oldContainer != null) {
			oldContainer.kill();
		}
	}

}
