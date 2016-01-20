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
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Strategy;
import io.mandrel.common.net.Uri;
import io.mandrel.data.Link;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.document.Document;
import io.mandrel.metadata.MetadataStores;
import io.mandrel.metrics.GlobalAccumulator;
import io.mandrel.metrics.SpiderAccumulator;
import io.mandrel.requests.Requester;
import io.mandrel.requests.Requesters;
import io.mandrel.transport.Clients;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.conn.ConnectTimeoutException;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.handler.timeout.WriteTimeoutException;
import org.springframework.util.StopWatch;

/**
 * Weeeeeeeeeeeeeeeeehhh!!
 */
@Slf4j
@RequiredArgsConstructor
public class Loop implements Runnable {

	private final ExtractorService extractorService;
	private final Spider spider;
	private final Clients clients;

	private final SpiderAccumulator spiderAccumulator;
	private final GlobalAccumulator globalAccumulator;

	private final AtomicBoolean run = new AtomicBoolean(false);

	public void start() {
		run.set(true);
	}

	public void pause() {
		run.set(false);
	}

	public boolean isRunning() {
		return run.get();
	}

	@Override
	public void run() {

		while (true) {

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

				// Take on elements
				// ListenableFuture<Uri> result =
				// clients.onRandomFrontier().map(frontier ->
				// frontier.next(spider.getId()));
				log.trace("> Asking for uri...");
				Uri uri = clients.onRandomFrontier().map(frontier -> frontier.next(spider.getId()));

				// TODO -> You can do better things than this...
				// Uri uri = result.get(20000, TimeUnit.MILLISECONDS);

				if (uri != null) {

					log.trace("> Getting uri {} !", uri);

					//
					StopWatch watch = new StopWatch();
					watch.start();

					//
					Optional<Requester<? extends Strategy>> requester = Requesters.of(spider.getId(), uri.getScheme());
					if (requester.isPresent()) {
						Requester<? extends Strategy> r = requester.get();
						try {
							Blob blob = r.get(uri);

							watch.stop();

							log.trace("> Start parsing data for {}", uri);

							blob.getMetadata().getFetchMetadata().setTimeToFetch(watch.getTotalTimeMillis());

							updateMetrics(watch, blob);

							Map<String, Instance<?>> cachedSelectors = new HashMap<>();
							if (spider.getExtractors() != null && spider.getExtractors().getPages() != null) {
								log.trace(">  - Extracting documents for {}...", uri);
								spider.getExtractors().getPages().forEach(ex -> {
									List<Document> documents = extractorService.extractThenFormatThenStore(spider.getId(), cachedSelectors, blob, ex);

									if (documents != null) {
										spiderAccumulator.incDocumentForExtractor(ex.getName(), documents.size());
									}
								});
								log.trace(">  - Extracting documents for {} done!", uri);
							}

							if (spider.getExtractors().getOutlinks() != null) {
								log.trace(">  - Extracting outlinks for {}...", uri);
								final Uri theUri = uri;
								spider.getExtractors()
										.getOutlinks()
										.forEach(
												ol -> {
													Set<Link> allFilteredOutlinks = extractorService.extractAndFilterOutlinks(spider, theUri, cachedSelectors,
															blob, ol).getRight();
													blob.getMetadata().getFetchMetadata().setOutlinks(allFilteredOutlinks);
													add(spider.getId(), allFilteredOutlinks.stream().map(l -> l.getUri()).collect(Collectors.toSet()));
												});
								log.trace(">  - Extracting outlinks done for {}!", uri);
							}

							BlobStores.get(spider.getId()).ifPresent(b -> b.putBlob(blob.getMetadata().getUri(), blob));

							log.trace(">  - Storing metadata for {}...", uri);
							MetadataStores.get(spider.getId()).addMetadata(blob.getMetadata().getUri(), blob.getMetadata().getFetchMetadata());
							log.trace(">  - Storing metadata for {} done!", uri);

							log.trace("> End parsing data for {}", uri);
						} catch (Exception t) {
							// TODO create and use internal exception instead...
							if (t instanceof ConnectTimeoutException) {
								spiderAccumulator.incConnectTimeout();
								add(spider.getId(), uri);
							} else if (t instanceof ReadTimeoutException) {
								spiderAccumulator.incReadTimeout();
								add(spider.getId(), uri);
							} else if (t instanceof ConnectException || t instanceof WriteTimeoutException || t instanceof TimeoutException
									|| t instanceof java.util.concurrent.TimeoutException) {
								spiderAccumulator.incConnectException();
								add(spider.getId(), uri);
							} else {
								log.debug("Error while looping", t);
							}
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
			} catch (Exception e) {
				// TODO This is ugly, but don't have the choice... Hope that a
				// new thrift client will throw a good TimeoutException
				if (e instanceof ExecutionException && e.getMessage().contains("Task timed out while executing.")) {
					log.debug("Time out when getting uri from frontier, waiting 20 sec", e.getMessage());
					try {
						TimeUnit.MILLISECONDS.sleep(20000);
					} catch (InterruptedException ie) {
						// Don't care
						log.trace("", ie);
					}
				} else {
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
	}

	public void updateMetrics(StopWatch watch, Blob blob) {
		spiderAccumulator.incNbPages();
		globalAccumulator.incNbPages();

		spiderAccumulator.incPageForStatus(blob.getMetadata().getFetchMetadata().getStatusCode());
		globalAccumulator.incPageForStatus(blob.getMetadata().getFetchMetadata().getStatusCode());

		spiderAccumulator.incPageForHost(blob.getMetadata().getUri().getHost());
		globalAccumulator.incPageForHost(blob.getMetadata().getUri().getHost());

		spiderAccumulator.incTotalTimeToFetch(watch.getLastTaskTimeMillis());

		if (blob.getMetadata().getSize() != null) {
			spiderAccumulator.incTotalSize(blob.getMetadata().getSize());
			globalAccumulator.incTotalSize(blob.getMetadata().getSize());
		}
	}

	public void add(long spiderId, Set<Uri> uris) {
		clients.onRandomFrontier().with(frontier -> frontier.mschedule(spiderId, uris));
	}

	public void add(long spiderId, Uri uri) {
		clients.onRandomFrontier().with(frontier -> frontier.schedule(spiderId, uri));
	}
}
