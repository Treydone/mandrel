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
///*
// * Licensed to Mandrel under one or more contributor
// * license agreements. See the NOTICE file distributed with
// * this work for additional information regarding copyright
// * ownership. Mandrel licenses this file to you under
// * the Apache License, Version 2.0 (the "License"); you may
// * not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package io.mandrel.frontier;
//
//import io.mandrel.common.data.Spider;
//import io.mandrel.data.content.selector.Selector.Instance;
//import io.mandrel.data.extract.ExtractorService;
//import io.mandrel.data.spider.Link;
//import io.mandrel.document.Document;
//import io.mandrel.due.DuplicateUrlEliminator;
//import io.mandrel.frontier.queue.QueueService;
//import io.mandrel.metrics.GlobalMetrics;
//import io.mandrel.metrics.MetricsService;
//import io.mandrel.metrics.SpiderMetrics;
//
//import java.net.ConnectException;
//import java.net.URI;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import javax.inject.Inject;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import org.jboss.netty.channel.ConnectTimeoutException;
//import org.jboss.netty.handler.timeout.ReadTimeoutException;
//import org.jboss.netty.handler.timeout.TimeoutException;
//import org.jboss.netty.handler.timeout.WriteTimeoutException;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StopWatch;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor(onConstructor = @__(@Inject))
//public class FrontierService {
//
//	private final QueueService queueService;
//
//	private final MetricsService metricsService;
//
//	private final ExtractorService extractorService;
//
//	private final DuplicateUrlEliminator duplicateUrlEliminator;
//
//	public void add(long spiderId, Set<URI> uris) {
//		queueService.add("uris-" + spiderId, uris);
//	}
//
//	public void add(long spiderId, URI uri) {
//		queueService.add("uris-" + spiderId, uri);
//	}
//
//	public void registrer(Spider spider) {
//		log.debug("Registering spider {} ({})", spider.getName(), spider.getId());
//
//		SpiderMetrics spiderMetrics = metricsService.spider(spider.getId());
//		GlobalMetrics globalMetrics = metricsService.global();
//		queueService.<String> registrer("uris-" + spider.getId(), uri -> {
//			long maxPages = spider.getClient().getPoliteness().getMaxPages();
//			if (maxPages > 0 && (spiderMetrics.getNbPages() + spiderMetrics.getNbPendingPages()) > maxPages) {
//				log.debug("Max pages reached for {} ({})", spider.getName(), spider.getId());
//				return true;
//			}
//			try {
//				doRequest(spider, new URI(uri), spiderMetrics, globalMetrics);
//			} catch (Exception e) {
//				log.debug("Well...?", e);
//			}
//			return false;
//		});
//	}
//
//	private void doRequest(Spider spider, URI uri, SpiderMetrics spiderMetrics, GlobalMetrics globalMetrics) {
//		try {
//			StopWatch watch = new StopWatch();
//			watch.start();
//
//			// Mark as pending
//			duplicateUrlEliminator.markAsPending("pendings-" + spider.getId(), uri);
//
//			spider.getClient()
//					.getRequester(uri.getScheme())
//					.get(uri,
//							spider,
//							blob -> {
//
//								watch.stop();
//
//								log.trace("> Start parsing data for {}", uri);
//
//								blob.metadata().fetchMetadata().timeToFetch(watch.getTotalTimeMillis());
//
//								spiderMetrics.incNbPages();
//								globalMetrics.incNbPages();
//
//								spiderMetrics.incPageForStatus(blob.metadata().fetchMetadata().statusCode());
//								globalMetrics.incPageForStatus(blob.metadata().fetchMetadata().statusCode());
//
//								spiderMetrics.incPageForHost(blob.metadata().uri().getHost());
//								globalMetrics.incPageForHost(blob.metadata().uri().getHost());
//
//								spiderMetrics.incTotalTimeToFetch(watch.getLastTaskTimeMillis());
//								spiderMetrics.incTotalSize(blob.metadata().size());
//								globalMetrics.incTotalSize(blob.metadata().size());
//
//								Map<String, Instance<?>> cachedSelectors = new HashMap<>();
//								if (spider.getExtractors() != null && spider.getExtractors().getPages() != null) {
//									log.trace(">  - Extracting documents for {}...", uri);
//									spider.getExtractors().getPages().forEach(ex -> {
//										List<Document> documents = extractorService.extractThenFormatThenStore(spider.getId(), cachedSelectors, blob, ex);
//
//										if (documents != null) {
//											spiderMetrics.incDocumentForExtractor(ex.getName(), documents.size());
//										}
//									});
//									log.trace(">  - Extracting documents for {} done!", uri);
//								}
//
//								if (spider.getExtractors().getOutlinks() != null) {
//									log.trace(">  - Extracting outlinks for {}...", uri);
//									spider.getExtractors()
//											.getOutlinks()
//											.forEach(
//													ol -> {
//														Set<Link> allFilteredOutlinks = extractorService.extractAndFilterOutlinks(spider, uri, cachedSelectors,
//																blob, ol).getRight();
//
//														blob.metadata().fetchMetadata().outlinks(allFilteredOutlinks);
//
//														// Respect politeness
//														// for this
//														// spider
//														// TODO
//
//														add(spider.getId(), duplicateUrlEliminator.filterPendings("pendings-" + spider.getId(),
//																allFilteredOutlinks.stream().map(l -> l.uri()).collect(Collectors.toSet())));
//													});
//									log.trace(">  - Extracting outlinks done for {}!", uri);
//								}
//
//								if (spider.getStores().getBlobStore() != null) {
//									log.trace(">  - Storing page {}...", uri);
//									spider.getStores().getBlobStore().putBlob(spider.getId(), blob.metadata().uri(), blob);
//									log.trace(">  - Storing page {} done!", uri);
//								}
//
//								log.trace(">  - Storing metadata for {}...", uri);
//								spider.getStores().getMetadataStore().addMetadata(spider.getId(), blob.metadata().uri(), blob.metadata().fetchMetadata());
//								log.trace(">  - Storing metadata for {} done!", uri);
//
//								duplicateUrlEliminator.removePending("pendings-" + spider.getId(), uri);
//
//								log.trace("> End parsing data for {}", uri);
//							},
//							t -> {
//								// Well...
//								if (t != null) {
//									if (t instanceof ConnectTimeoutException) {
//										spiderMetrics.incConnectTimeout();
//										add(spider.getId(), uri);
//									} else if (t instanceof ReadTimeoutException) {
//										spiderMetrics.incReadTimeout();
//										add(spider.getId(), uri);
//									} else if (t instanceof ConnectException || t instanceof WriteTimeoutException || t instanceof TimeoutException
//											|| t instanceof java.util.concurrent.TimeoutException) {
//										spiderMetrics.incConnectException();
//										add(spider.getId(), uri);
//									}
//								} else {
//									add(spider.getId(), uri);
//								}
//
//								duplicateUrlEliminator.removePending("pendings-" + spider.getId(), uri);
//							});
//		} catch (Exception e) {
//			duplicateUrlEliminator.removePending("pendings-" + spider.getId(), uri);
//			log.debug("Can not fetch uri {} due to {}", new Object[] { uri, e.toString() }, e);
//		}
//	}
//
//}
