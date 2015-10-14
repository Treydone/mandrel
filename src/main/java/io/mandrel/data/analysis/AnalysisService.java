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

import io.mandrel.blob.Blob;
import io.mandrel.common.data.Spider;
import io.mandrel.common.robots.ExtendedRobotRules;
import io.mandrel.common.robots.RobotsTxtUtils;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.data.spider.InitService;
import io.mandrel.data.spider.Link;
import io.mandrel.document.Document;
import io.mandrel.requests.Requester;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnalysisService {

	private final ExtractorService extractorService;
	private final Requester requester;
	private final InitService initService;

	public Analysis analyze(Spider spider, String source) {
		Blob blob;
		try {
			blob = requester.getBlocking(new URI(source), spider);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		log.trace("Getting response for {}", source);

		Analysis report = buildReport(spider, blob);
		return report;
	}

	protected Analysis buildReport(Spider spider, Blob blob) {

		initService.injectAndInit(spider);

		Analysis report = new Analysis();
		if (spider.getExtractors() != null) {
			Map<String, Instance<?>> cachedSelectors = new HashMap<>();

			// Page extraction
			if (spider.getExtractors().getPages() != null) {
				Map<String, List<Document>> documentsByExtractor = spider.getExtractors().getPages().stream()
						.map(ex -> Pair.of(ex.getName(), extractorService.extractThenFormat(cachedSelectors, blob, ex)))
						.filter(pair -> pair != null && pair.getKey() != null && pair.getValue() != null)
						.collect(Collectors.toMap(key -> key.getLeft(), value -> value.getRight()));
				report.documents(documentsByExtractor);
			}

			// Link extraction
			if (spider.getExtractors().getOutlinks() != null) {
				Map<String, Pair<Set<Link>, Set<Link>>> outlinksByExtractor = spider.getExtractors().getOutlinks().stream().map(ol -> {
					return Pair.of(ol.getName(), extractorService.extractAndFilterOutlinks(spider, blob.metadata().uri(), cachedSelectors, blob, ol));
				}).collect(Collectors.toMap(key -> key.getLeft(), value -> value.getRight()));

				report.outlinks(Maps.transformEntries(outlinksByExtractor, (key, entries) -> entries.getLeft()));
				report.filteredOutlinks(Maps.transformEntries(outlinksByExtractor, (key, entries) -> entries.getRight()));
			}

			// Robots.txt
			URI pageURL = blob.metadata().uri();
			String robotsTxtUrl = pageURL.getScheme() + "://" + pageURL.getHost() + ":" + pageURL.getPort() + "/robots.txt";
			ExtendedRobotRules robotRules = RobotsTxtUtils.getRobotRules(robotsTxtUrl);
			report.robotRules(robotRules);

			// Sitemaps
			if (robotRules != null && robotRules.getSitemaps() != null) {
				Map<String, List<AbstractSiteMap>> sitemaps = new HashMap<>();
				robotRules.getSitemaps().forEach(url -> {
					List<AbstractSiteMap> results = getSitemapsForUrl(url);
					sitemaps.put(url, results);
				});
				report.sitemaps(sitemaps);
			}
		}

		report.metadata(blob.metadata());
		return report;
	}

	public List<AbstractSiteMap> getSitemapsForUrl(String sitemapUrl) {
		List<AbstractSiteMap> sitemaps = new ArrayList<>();

		SiteMapParser siteMapParser = new SiteMapParser();
		try {
			Blob blob = requester.getBlocking(new URI(sitemapUrl));
			String contentType = blob.metadata().contentMetadata().contentType() != null ? blob.metadata().contentMetadata().contentType() : "text/xml";

			AbstractSiteMap sitemap = siteMapParser.parseSiteMap(contentType, IOUtils.toByteArray(blob.payload().openStream()), new URL(sitemapUrl));

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
