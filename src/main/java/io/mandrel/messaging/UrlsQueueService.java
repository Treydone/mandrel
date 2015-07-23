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
package io.mandrel.messaging;

import io.mandrel.common.data.Spider;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.due.DuplicateUrlEliminator;
import io.mandrel.gateway.Document;
import io.mandrel.http.Metadata;
import io.mandrel.metrics.GlobalMetrics;
import io.mandrel.metrics.MetricsService;
import io.mandrel.metrics.SpiderMetrics;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jboss.netty.channel.ConnectTimeoutException;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.handler.timeout.TimeoutException;
import org.jboss.netty.handler.timeout.WriteTimeoutException;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UrlsQueueService {

	private final QueueService queueService;

	private final MetricsService metricsService;

	private final ExtractorService extractorService;

	private final DuplicateUrlEliminator duplicateUrlEliminator;

	public void add(long spiderId, Set<String> urls) {
		queueService.add("urls-" + spiderId, urls);
	}

	public void add(long spiderId, String url) {
		queueService.add("urls-" + spiderId, url);
	}

	public void registrer(Spider spider) {
		log.debug("Registering spider {} ({})", spider.getName(), spider.getId());

		SpiderMetrics spiderMetrics = metricsService.spider(spider.getId());
		GlobalMetrics globalMetrics = metricsService.global();
		queueService.<String> registrer("urls-" + spider.getId(), url -> {
			long maxPages = spider.getClient().getStrategy().getPoliteness().getMaxPages();
			if (maxPages > 0 && (spiderMetrics.getNbPages() + spiderMetrics.getNbPendingPages()) > maxPages) {
				log.debug("Max pages reached for {} ({})", spider.getName(), spider.getId());
				return true;
			}
			doRequest(spider, url, spiderMetrics, globalMetrics);
			return false;
		});
	}

	private void doRequest(Spider spider, String url, SpiderMetrics spiderMetrics, GlobalMetrics globalMetrics) {
		try {
			StopWatch watch = new StopWatch();
			watch.start();

			// Mark as pending
			duplicateUrlEliminator.markAsPending("pendings-" + spider.getId(), url, Boolean.TRUE);

			spider.getClient()
					.getRequester()
					.get(url,
							spider,
							webPage -> {

								watch.stop();

								log.trace("> Start parsing data for {}", url);

								Metadata metadata = webPage.getMetadata();
								metadata.setTimeToFetch(watch.getTotalTimeMillis());

								spiderMetrics.incNbPages();
								globalMetrics.incNbPages();
								
								spiderMetrics.incPageForStatus(metadata.getStatusCode());
								globalMetrics.incPageForStatus(metadata.getStatusCode());
								
								spiderMetrics.incPageForHost(metadata.getUrl().getHost());
								globalMetrics.incPageForHost(metadata.getUrl().getHost());
								
								spiderMetrics.incTotalTimeToFetch(watch.getLastTaskTimeMillis());
								spiderMetrics.incTotalSize(webPage.getBody().length);
								globalMetrics.incTotalSize(webPage.getBody().length);

								Map<String, Instance<?>> cachedSelectors = new HashMap<>();
								if (spider.getExtractors() != null && spider.getExtractors().getPages() != null) {
									log.trace(">  - Extracting documents for {}...", url);
									spider.getExtractors().getPages().forEach(ex -> {
										List<Document> documents = extractorService.extractThenFormatThenStore(spider.getId(), cachedSelectors, webPage, ex);

										if (documents != null) {
											spiderMetrics.incDocumentForExtractor(ex.getName(), documents.size());
										}
									});
									log.trace(">  - Extracting documents for {} done!", url);
								}

								if (spider.getExtractors().getOutlinks() != null) {
									log.trace(">  - Extracting outlinks for {}...", url);
									spider.getExtractors()
											.getOutlinks()
											.forEach(
													ol -> {
														Set<String> allFilteredOutlinks = extractorService.extractAndFilterOutlinks(spider, url,
																cachedSelectors, webPage, ol).getRight();

														metadata.setOutlinks(allFilteredOutlinks);

														// Respect politeness
														// for this
														// spider
														// TODO

														allFilteredOutlinks = duplicateUrlEliminator.filterPendings("pendings-" + spider.getId(),
																allFilteredOutlinks);
														add(spider.getId(), allFilteredOutlinks);
													});
									log.trace(">  - Extracting outlinks done for {}!", url);
								}

								if (spider.getStores().getPageStore() != null) {
									log.trace(">  - Storing page {}...", url);
									spider.getStores().getPageStore().addPage(spider.getId(), webPage.getUrl().toString(), webPage);
									log.trace(">  - Storing page {} done!", url);
								}

								log.trace(">  - Storing metadata for {}...", url);
								spider.getStores().getPageMetadataStore().addMetadata(spider.getId(), webPage.getUrl().toString(), metadata);
								log.trace(">  - Storing metadata for {} done!", url);

								duplicateUrlEliminator.removePending("pendings-" + spider.getId(), url);

								log.trace("> End parsing data for {}", url);
							}, t -> {
								// Well...
							if (t != null) {
								if (t instanceof ConnectTimeoutException) {
									spiderMetrics.incConnectTimeout();
									add(spider.getId(), url);
								} else if (t instanceof ReadTimeoutException) {
									spiderMetrics.incReadTimeout();
									add(spider.getId(), url);
								} else if (t instanceof ConnectException || t instanceof WriteTimeoutException || t instanceof TimeoutException
										|| t instanceof java.util.concurrent.TimeoutException) {
									spiderMetrics.incConnectException();
									add(spider.getId(), url);
								}
							} else {
								add(spider.getId(), url);
							}

							duplicateUrlEliminator.removePending("pendings-" + spider.getId(), url);
						});
		} catch (Exception e) {
			duplicateUrlEliminator.removePending("pendings-" + spider.getId(), url);
			log.debug("Can not fetch url {} due to {}", new Object[] { url, e.toString() }, e);
		}
	}

}
