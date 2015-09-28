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
package io.mandrel.data.analysis;

import io.mandrel.common.data.Spider;
import io.mandrel.common.robots.ExtendedRobotRules;
import io.mandrel.common.robots.RobotsTxtUtils;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.data.spider.Link;
import io.mandrel.gateway.Document;
import io.mandrel.requests.Requester;
import io.mandrel.requests.http.HttpMetadata;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnalysisService {

	private final ExtractorService extractorService;

	private final Requester<HttpMetadata> requester;

	public Optional<Analysis> analyze(Long id, String source) {
		return get(id).map(spider -> {

			HttpMetadata webPage;
			try {
				webPage = requester.getBlocking(new URI(source), spider);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
			log.trace("Getting response for {}", source);

			Analysis report = buildReport(spider, webPage);

			return report;
		});
	}

	protected Analysis buildReport(Spider spider, HttpMetadata webPage) {

		injectAndInit(spider);

		Analysis report = new Analysis();
		if (spider.getExtractors() != null) {
			Map<String, Instance<?>> cachedSelectors = new HashMap<>();

			// Page extraction
			if (spider.getExtractors().getPages() != null) {
				Map<String, List<Document>> documentsByExtractor = spider.getExtractors().getPages().stream()
						.map(ex -> Pair.of(ex.getName(), extractorService.extractThenFormat(cachedSelectors, webPage, ex)))
						.filter(pair -> pair != null && pair.getKey() != null && pair.getValue() != null)
						.collect(Collectors.toMap(key -> key.getLeft(), value -> value.getRight()));
				report.setDocuments(documentsByExtractor);
			}

			// Link extraction
			if (spider.getExtractors().getOutlinks() != null) {
				Map<String, Pair<Set<Link>, Set<String>>> outlinksByExtractor = spider.getExtractors().getOutlinks().stream().map(ol -> {
					return Pair.of(ol.getName(), extractorService.extractAndFilterOutlinks(spider, webPage.getUrl().toString(), cachedSelectors, webPage, ol));
				}).collect(Collectors.toMap(key -> key.getLeft(), value -> value.getRight()));

				report.setOutlinks(Maps.transformEntries(outlinksByExtractor, (key, entries) -> entries.getLeft()));
				report.setFilteredOutlinks(Maps.transformEntries(outlinksByExtractor, (key, entries) -> entries.getRight()));
			}

			// Robots.txt
			URI pageURL = webPage.getUri();
			String robotsTxtUrl = pageURL.getScheme() + "://" + pageURL.getHost() + ":" + pageURL.getPort() + "/robots.txt";
			ExtendedRobotRules robotRules = RobotsTxtUtils.getRobotRules(robotsTxtUrl);
			report.setRobotRules(robotRules);

			// Sitemaps
			if (robotRules != null && robotRules.getSitemaps() != null) {
				Map<String, List<AbstractSiteMap>> sitemaps = new HashMap<>();
				robotRules.getSitemaps().forEach(url -> {
					List<AbstractSiteMap> results = getSitemapsForUrl(url);
					sitemaps.put(url, results);
				});
				report.setSitemaps(sitemaps);
			}
		}

		report.setMetadata(webPage.getMetadata());
		return report;
	}

	public List<AbstractSiteMap> getSitemapsForUrl(String sitemapUrl) {
		List<AbstractSiteMap> sitemaps = new ArrayList<>();

		SiteMapParser siteMapParser = new SiteMapParser();
		try {
			HttpMetadata page = requester.getBlocking(new URI(sitemapUrl));
			List<String> headers = page.getHeaders().get(HttpHeaders.CONTENT_TYPE);
			String contentType = headers != null && headers.size() > 0 ? headers.get(0) : "text/xml";

			AbstractSiteMap sitemap = siteMapParser.parseSiteMap(contentType, page.getBody(), new URL(sitemapUrl));

			if (sitemap.isIndex()) {
				sitemaps.addAll(((SiteMapIndex) sitemap).getSitemaps());
			} else {
				sitemaps.add(sitemap);
			}
		} catch (Exception e) {
			log.debug("", e);
		}
		return sitemaps;
	}
}
