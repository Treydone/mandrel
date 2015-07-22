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
package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.common.data.State;
import io.mandrel.common.robots.ExtendedRobotRules;
import io.mandrel.common.robots.RobotsTxtUtils;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.data.filters.link.AllowedForDomainsFilter;
import io.mandrel.data.filters.link.SkipAncorFilter;
import io.mandrel.data.filters.link.UrlPatternFilter;
import io.mandrel.data.source.FixedSource;
import io.mandrel.data.source.Source;
import io.mandrel.gateway.Document;
import io.mandrel.http.Requester;
import io.mandrel.http.WebPage;
import io.mandrel.task.TaskService;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.kohsuke.randname.RandomNameGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpiderService {

	private final SpiderRepository spiderRepository;

	private final TaskService taskService;

	private final ExtractorService extractorService;

	private final Requester requester;

	private final HazelcastInstance instance;

	private Validator spiderValidator = new SpiderValidator();

	private RandomNameGenerator generator = new RandomNameGenerator();

	/**
	 * On start:
	 * <ul>
	 * <li>Find the available spiders, and foreach spider:
	 * <li>Start the spider
	 * <li>Start the sources
	 * </ul>
	 */
	@PostConstruct
	public void init() {

		// Start the available spiders on this node
		spiderRepository.list().filter(spider -> State.STARTED.equals(spider.getState())).forEach(spider -> {
			taskService.executeOnLocalMember(String.valueOf(spider.getId()), new SpiderTask(spider));

			// TODO manage sources!
			// spider.getSources().stream().forEach(prepareSource(spider.getId(),
			// spider));
			});
	}

	public void injectAndInit(Spider spider) {
		spider.getStores().getPageMetadataStore().setHazelcastInstance(instance);
		if (spider.getStores().getPageStore() != null) {
			spider.getStores().getPageStore().setHazelcastInstance(instance);
		}
		if (spider.getExtractors() != null && spider.getExtractors().getPages() != null) {
			spider.getExtractors().getPages().forEach(ex -> ex.getDocumentStore().setHazelcastInstance(instance));
		}

		// TODO
		Map<String, Object> properties = new HashMap<>();

		spider.getStores().getPageMetadataStore().init(properties);
		if (spider.getStores().getPageStore() != null) {
			spider.getStores().getPageStore().init(properties);
		}
	}

	public BindingResult validate(Spider spider) {
		BindingResult errors = new BeanPropertyBindingResult(spider, "spider");
		spiderValidator.validate(spider, errors);
		return errors;
	}

	public Spider update(Spider spider) throws BindException {
		BindingResult errors = validate(spider);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			throw new BindException(errors);
		}

		return spiderRepository.update(spider);
	}

	/**
	 * Create a new spider from a fixed list of urls.
	 * 
	 * @param urls
	 * @return
	 */
	public Spider add(List<String> urls) throws BindException {
		Spider spider = new Spider();
		spider.setName(generator.next());

		// Add source
		spider.setSources(Arrays.asList(new FixedSource().setUrls(urls).setName("fixed_source")));

		// Add filters
		spider.getFilters().getForLinks().add(new AllowedForDomainsFilter().setDomains(urls.stream().map(url -> {
			try {
				return new URL(url).getHost();
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}).collect(Collectors.toList())));
		spider.getFilters().getForLinks().add(new SkipAncorFilter());
		spider.getFilters().getForLinks().add(UrlPatternFilter.STATIC);

		return add(spider);
	}

	public Spider add(Spider spider) throws BindException {
		BindingResult errors = validate(spider);

		if (errors.hasErrors()) {
			errors.getAllErrors().stream().forEach(oe -> log.info(oe.toString()));
			throw new BindException(errors);
		}

		spider = spiderRepository.add(spider);

		return spider;
	}

	public Optional<Spider> get(long id) {
		return spiderRepository.get(id);
	}

	public Stream<Spider> list() {
		return spiderRepository.list();
	}

	public Optional<Spider> start(long spiderId) {
		return get(spiderId).map(spider -> {

			if (State.STARTED.equals(spider.getState())) {
				return spider;
			}

			if (State.CANCELLED.equals(spider.getState())) {
				throw new RuntimeException("Spider cancelled!");
			}

			// TODO
			// if (spider.getClient().getPoliteness().isUseSitemaps()) {
			// spider.getSources().add(new SitemapsSource(url));
			// }

				spider.getSources().stream().filter(s -> s.check()).forEach(prepareSource(spiderId, spider));
				
				taskService.prepareSimpleExecutor(String.valueOf(spiderId));
				taskService.executeOnAllMembers(String.valueOf(spiderId), new SpiderTask(spider));

				spider.setState(State.STARTED);
				spiderRepository.update(spider);
				return spider;

			});
	}

	private Consumer<? super Source> prepareSource(long spiderId, Spider spider) {
		return source -> {
			String sourceExecServiceName = spiderId + "-source-" + source.getName();

			taskService.prepareSimpleExecutor(String.valueOf(sourceExecServiceName));

			if (source.singleton()) {
				log.debug("Sourcing from a random member");
				taskService.executeOnRandomMember(sourceExecServiceName, new SourceTask(spider.getId(), source));
			} else {
				log.debug("Sourcing from all members");
				taskService.executeOnAllMembers(sourceExecServiceName, new SourceTask(spider.getId(), source));
			}
		};
	}

	public Optional<Spider> cancel(long spiderId) {
		return get(spiderId).map(spider -> {

			taskService.shutdownAllExecutorService(spider);

			// Update status
				spider.setState(State.CANCELLED);
				return spiderRepository.update(spider);
			});
	}

	public Optional<Spider> end(long spiderId) {
		return get(spiderId).map(spider -> {

			taskService.shutdownAllExecutorService(spider);

			// Update status
				spider.setState(State.ENDED);
				return spiderRepository.update(spider);
			});
	}

	public Optional<Spider> delete(long spiderId) {
		return get(spiderId).map(spider -> {
			taskService.shutdownAllExecutorService(spider);

			// Delete data
				spider.getStores().getPageStore().deleteAllFor(spiderId);
				spider.getExtractors().getPages().stream().forEach(ex -> ex.getDocumentStore().deleteAllFor(spiderId));

				// Remove spider
				spiderRepository.delete(spiderId);

				return spider;
			});
	}

	public Optional<Analysis> analyze(Long id, String source) {
		return get(id).map(spider -> {

			WebPage webPage;
			try {
				webPage = requester.getBlocking(source, spider);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
			log.trace("Getting response for {}", source);

			Analysis report = buildReport(spider, webPage);

			return report;
		});
	}

	protected Analysis buildReport(Spider spider, WebPage webPage) {

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
			URL pageURL = webPage.getUrl();
			String robotsTxtUrl = pageURL.getProtocol() + "://" + pageURL.getHost() + ":" + pageURL.getPort() + "/robots.txt";
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
			WebPage page = requester.getBlocking(sitemapUrl);
			List<String> headers = page.getMetadata().getHeaders().get(HttpHeaders.CONTENT_TYPE);
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
