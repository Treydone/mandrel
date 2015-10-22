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
import io.mandrel.common.container.Container;
import io.mandrel.common.data.Spider;
import io.mandrel.common.service.TaskContext;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.data.source.Source;
import io.mandrel.document.DocumentStore;
import io.mandrel.document.DocumentStores;
import io.mandrel.frontier.FrontierClient;
import io.mandrel.metadata.MetadataStore;
import io.mandrel.metadata.MetadataStores;
import io.mandrel.metrics.MetricsService;
import io.mandrel.requests.Requester;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import com.hazelcast.core.HazelcastInstance;

@Data
@Slf4j
@RequiredArgsConstructor
public class WorkerContainer implements Container {

	private final ExtractorService extractorService;
	private final MetricsService metricsService;
	private final Spider spider;
	private final FrontierClient frontierClient;
	private final HazelcastInstance instance;

	private ExecutorService executor;

	public void start() {

		// Create the thread factory
		BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("workerthread-%d").daemon(true).priority(Thread.MAX_PRIORITY).build();

		// Get number of parallel loops
		int parallel = 0;
		executor = Executors.newFixedThreadPool(parallel, threadFactory);

		// spider.getClient().

		// Create loop
		IntStream.range(0, parallel).forEach(idx -> {
			executor.submit(new Loop(extractorService, spider, null, metricsService.spider(spider.getId()), metricsService.global()));
		});

		// Create context
		TaskContext context = new TaskContext();
		context.setDefinition(spider);
		context.setInstance(instance);

		// Init stores
		MetadataStore metadatastore = spider.getStores().getMetadataStore().build(context);
		metadatastore.init();
		MetadataStores.add(spider.getId(), metadatastore);

		BlobStore blobStore = spider.getStores().getBlobStore().build(context);
		blobStore.init();
		BlobStores.add(spider.getId(), blobStore);

		spider.getExtractors().getPages().forEach(ex -> {
			DocumentStore documentStore = ex.getDocumentStore().build(context);
			documentStore.init();
			DocumentStores.add(spider.getId(), ex.getName(), documentStore);
		});

		// Init sources
		spider.getSources().forEach(s -> {
			Source source = s.build(context);
			// TODO
				if (source.check()) {
					if (source.singleton()) {

					}
				}
			});

		// Init requesters
		for (Requester requester : spider.getClient().getRequesters()) {
			// Prepare client
			if (requester.getStrategy().getNameResolver() != null) {
				requester.getStrategy().getNameResolver().init();
			}
			if (requester.getStrategy().getProxyServersSource() != null) {
				requester.getStrategy().getProxyServersSource().init();
			}
			requester.init();
		}
	}

	@Override
	public String type() {
		return "worker";
	}

	@Override
	public void pause() {

	}

	@Override
	public void kill() {

		try {
			MetadataStores.remove(spider.getId());
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}

		BlobStores.remove(spider.getId());

		DocumentStores.remove(spider.getId());

		executor.shutdownNow();
	}

	public void register() {
		WorkerContainers.add(spider.getId(), this);
	}

	public void unregister() {
		WorkerContainers.remove(spider.getId());
	}
}
