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

import io.mandrel.blob.Blob;
import io.mandrel.blob.BlobStores;
import io.mandrel.common.data.Job;
import io.mandrel.common.net.Uri;
import io.mandrel.data.Link;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.document.Document;
import io.mandrel.endpoints.contracts.frontier.Next;
import io.mandrel.metadata.MetadataStores;
import io.mandrel.metrics.GlobalAccumulator;
import io.mandrel.metrics.JobAccumulator;
import io.mandrel.requests.ConnectTimeoutException;
import io.mandrel.requests.ReadTimeoutException;
import io.mandrel.requests.Requester;
import io.mandrel.requests.Requesters;
import io.mandrel.transport.MandrelClient;
import io.mandrel.transport.RemoteException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StopWatch;

/**
 * Weeeeeeeeeeeeeeeeehhh!!
 */
@Slf4j
@RequiredArgsConstructor
public class Loop implements Runnable {

	private final ExtractorService extractorService;
	private final Job job;
	private final MandrelClient client;

	private final JobAccumulator jobAccumulator;
	private final GlobalAccumulator globalAccumulator;

	private final Barrier barrier;

	private final AtomicBoolean run = new AtomicBoolean(false);
	private final AtomicBoolean loop = new AtomicBoolean(true);

	public void start() {
		run.set(true);
		loop.set(true);
	}

	public void pause() {
		run.set(false);
		loop.set(true);
	}

	public void stop() {
		run.set(false);
		loop.set(false);
	}

	public boolean isRunning() {
		return run.get() && loop.get();
	}

	@Override
	public void run() {
		while (loop.get()) {

			try {
				if (!run.get()) {
					log.trace("Waiting...");
					try {
						TimeUnit.MILLISECONDS.sleep(2000);
					} catch (InterruptedException e) {
						// Don't care
						log.trace("", e);
					}
					continue;
				}

				log.trace("> Asking for uri...");
				Next next = client.frontier().client().onAny().map(frontier -> frontier.next(job.getId())).get(20000, TimeUnit.MILLISECONDS);
				Uri uri = next.getUri();

				if (uri != null) {

					log.trace("> Getting uri {} !", uri);

					//
					StopWatch watch = new StopWatch();
					watch.start();

					//
					Optional<Requester> requester = Requesters.of(job.getId(), uri.getScheme());
					if (requester.isPresent()) {
						Requester r = requester.get();

						Blob blob = null;
						try {
							blob = processBlob(uri, watch, r);
						} catch (Exception t) {
							// TODO create and use internal exception instead...
							if (t instanceof ConnectTimeoutException) {
								jobAccumulator.incConnectTimeout();
								add(job.getId(), uri);
							} else if (t instanceof ReadTimeoutException) {
								jobAccumulator.incReadTimeout();
								add(job.getId(), uri);
							} else {
								log.debug("Error while looping", t);
							}
						} finally {
							barrier.passOrWait(blob != null && blob.getMetadata() != null ? blob.getMetadata().getSize() : null);
						}
					} else {
						// TODO Unknown protocol
						log.debug("Unknown protocol, can not find requester for '{}'", uri.getScheme());
					}
				} else {
					log.trace("Frontier returned null Uri, waiting");
					try {
						TimeUnit.MILLISECONDS.sleep(10000);
					} catch (InterruptedException e) {
						// Don't care
						log.trace("", e);
					}
				}
			} catch (RemoteException e) {
				switch (e.getError()) {
				case G_UNKNOWN:
					log.warn("Got a problem, waiting 2 sec...", e);
					try {
						TimeUnit.MILLISECONDS.sleep(2000);
					} catch (InterruptedException ie) {
						// Don't care
						log.trace("", ie);
					}
				}
			} catch (Exception e) {
				log.warn("Got a problem, waiting 2 sec...", e);
				try {
					TimeUnit.MILLISECONDS.sleep(2000);
				} catch (InterruptedException ie) {
					// Don't care
					log.trace("", ie);
				}
			}
		}
	}

	protected Blob processBlob(Uri uri, StopWatch watch, Requester r) throws Exception {
		Blob blob;
		blob = r.get(uri);

		watch.stop();

		log.trace("> Start parsing data for {}", uri);

		blob.getMetadata().getFetchMetadata().setTimeToFetch(watch.getTotalTimeMillis());

		updateMetrics(watch, blob);

		Map<String, Instance<?>> cachedSelectors = new HashMap<>();
		if (job.getExtractors() != null && job.getExtractors().getData() != null) {
			log.trace(">  - Extracting documents for {}...", uri);
			job.getExtractors().getData().forEach(ex -> {
				List<Document> documents = extractorService.extractThenFormatThenStore(job.getId(), cachedSelectors, blob, ex);

				if (documents != null) {
					jobAccumulator.incDocumentForExtractor(ex.getName(), documents.size());
				}
			});
			log.trace(">  - Extracting documents for {} done!", uri);
		}

		if (job.getExtractors().getOutlinks() != null) {
			log.trace(">  - Extracting outlinks for {}...", uri);
			final Uri theUri = uri;
			job.getExtractors().getOutlinks().forEach(ol -> {
				Set<Link> allFilteredOutlinks = extractorService.extractAndFilterOutlinks(job, theUri, cachedSelectors, blob, ol).getRight();
				blob.getMetadata().getFetchMetadata().setOutlinks(allFilteredOutlinks);
				add(job.getId(), allFilteredOutlinks.stream().map(l -> l.getUri()).collect(Collectors.toSet()));
			});
			log.trace(">  - Extracting outlinks done for {}!", uri);
		}

		BlobStores.get(job.getId()).ifPresent(b -> b.putBlob(blob.getMetadata().getUri(), blob));

		log.trace(">  - Storing metadata for {}...", uri);
		MetadataStores.get(job.getId()).addMetadata(blob.getMetadata().getUri(), blob.getMetadata());
		log.trace(">  - Storing metadata for {} done!", uri);

		log.trace("> End parsing data for {}", uri);
		return blob;
	}

	protected void updateMetrics(StopWatch watch, Blob blob) {
		jobAccumulator.incNbPages();
		globalAccumulator.incNbPages();

		jobAccumulator.incPageForStatus(blob.getMetadata().getFetchMetadata().getStatusCode());
		globalAccumulator.incPageForStatus(blob.getMetadata().getFetchMetadata().getStatusCode());

		jobAccumulator.incPageForHost(blob.getMetadata().getUri().getHost());
		globalAccumulator.incPageForHost(blob.getMetadata().getUri().getHost());

		jobAccumulator.incTotalTimeToFetch(watch.getLastTaskTimeMillis());

		if (blob.getMetadata().getSize() != null) {
			jobAccumulator.incTotalSize(blob.getMetadata().getSize());
			globalAccumulator.incTotalSize(blob.getMetadata().getSize());
		}
	}

	protected void add(long jobId, Set<Uri> uris) {
		if (CollectionUtils.isNotEmpty(uris)) {
			client.frontier().client().onAny().with(frontier -> frontier.mschedule(jobId, uris));
		}
	}

	protected void add(long jobId, Uri uri) {
		if (uri != null) {
			client.frontier().client().onAny().with(frontier -> frontier.schedule(jobId, uri));
		}
	}
}
