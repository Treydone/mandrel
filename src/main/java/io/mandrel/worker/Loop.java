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
import io.mandrel.data.Link;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.document.Document;
import io.mandrel.frontier.FrontierClient;
import io.mandrel.metadata.MetadataStores;
import io.mandrel.metrics.GlobalMetrics;
import io.mandrel.metrics.SpiderMetrics;
import io.mandrel.requests.Requester;

import java.net.ConnectException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jboss.netty.channel.ConnectTimeoutException;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.handler.timeout.TimeoutException;
import org.jboss.netty.handler.timeout.WriteTimeoutException;
import org.springframework.util.StopWatch;

/**
 * Weeeeeeeeeeeeeeeeehhh!!
 */
@Data
@Slf4j
@RequiredArgsConstructor
public class Loop implements Runnable {

	private final ExtractorService extractorService;
	private final Spider spider;
	private final FrontierClient frontierClient;

	private final SpiderMetrics spiderMetrics;
	private final GlobalMetrics globalMetrics;

	@Override
	public void run() {

		while (true) {

			URI uri = null;
			try {
				// Take on elements
				// uri = ...

				//
				StopWatch watch = new StopWatch();
				watch.start();

				//
				Requester requester = spider.getClient().getRequester(uri.getScheme());

				Blob blob = requester.getBlocking(uri);

				try {

					watch.stop();

					log.trace("> Start parsing data for {}", uri);

					blob.metadata().fetchMetadata().timeToFetch(watch.getTotalTimeMillis());

					spiderMetrics.incNbPages();
					globalMetrics.incNbPages();

					spiderMetrics.incPageForStatus(blob.metadata().fetchMetadata().statusCode());
					globalMetrics.incPageForStatus(blob.metadata().fetchMetadata().statusCode());

					spiderMetrics.incPageForHost(blob.metadata().uri().getHost());
					globalMetrics.incPageForHost(blob.metadata().uri().getHost());

					spiderMetrics.incTotalTimeToFetch(watch.getLastTaskTimeMillis());
					spiderMetrics.incTotalSize(blob.metadata().size());
					globalMetrics.incTotalSize(blob.metadata().size());

					Map<String, Instance<?>> cachedSelectors = new HashMap<>();
					if (spider.getExtractors() != null && spider.getExtractors().getPages() != null) {
						log.trace(">  - Extracting documents for {}...", uri);
						spider.getExtractors().getPages().forEach(ex -> {
							List<Document> documents = extractorService.extractThenFormatThenStore(spider.getId(), cachedSelectors, blob, ex);

							if (documents != null) {
								spiderMetrics.incDocumentForExtractor(ex.getName(), documents.size());
							}
						});
						log.trace(">  - Extracting documents for {} done!", uri);
					}

					if (spider.getExtractors().getOutlinks() != null) {
						log.trace(">  - Extracting outlinks for {}...", uri);
						spider.getExtractors().getOutlinks().forEach(ol -> {
							Set<Link> allFilteredOutlinks = extractorService.extractAndFilterOutlinks(spider, uri, cachedSelectors, blob, ol).getRight();
							blob.metadata().fetchMetadata().outlinks(allFilteredOutlinks);
							add(spider.getId(), allFilteredOutlinks.stream().map(l -> l.uri()).collect(Collectors.toSet()));
						});
						log.trace(">  - Extracting outlinks done for {}!", uri);
					}

					BlobStores.get(spider.getId()).ifPresent(b -> b.putBlob(blob.metadata().uri(), blob));

					log.trace(">  - Storing metadata for {}...", uri);
					MetadataStores.get(spider.getId()).addMetadata(blob.metadata().uri(), blob.metadata().fetchMetadata());
					log.trace(">  - Storing metadata for {} done!", uri);

					log.trace("> End parsing data for {}", uri);
				} catch (Exception t) {
					// Well...
					if (t instanceof ConnectTimeoutException) {
						spiderMetrics.incConnectTimeout();
						add(spider.getId(), uri);
					} else if (t instanceof ReadTimeoutException) {
						spiderMetrics.incReadTimeout();
						add(spider.getId(), uri);
					} else if (t instanceof ConnectException || t instanceof WriteTimeoutException || t instanceof TimeoutException
							|| t instanceof java.util.concurrent.TimeoutException) {
						spiderMetrics.incConnectException();
						add(spider.getId(), uri);
					}

				}
			} catch (Exception e) {
				if (uri != null) {
					log.debug("Can not fetch uri {} due to {}", new Object[] { uri, e.toString() }, e);
				} else {
					log.warn(e.getMessage(), e);
				}
			}
		}
	}

	public void add(long spiderId, Set<URI> uris) {
		frontierClient.add(spiderId, uris);
	}

	public void add(long spiderId, URI uri) {
		frontierClient.add(spiderId, uri);
	}
}
