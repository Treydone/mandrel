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

import io.mandrel.common.data.Constants;
import io.mandrel.common.data.Politeness;
import io.mandrel.common.data.Spider;
import io.mandrel.data.analysis.Analysis;
import io.mandrel.data.content.selector.SelectorService;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.data.filters.link.AllowedForDomainsFilter;
import io.mandrel.gateway.MetadataStore;
import io.mandrel.gateway.BlobStore;
import io.mandrel.requests.WebPage;
import io.mandrel.requests.http.HttpRequester;
import io.mandrel.script.ScriptingService;
import io.mandrel.timeline.TimelineService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

@RunWith(MockitoJUnitRunner.class)
public class SpiderServiceIntegrationTest {

	@Mock
	private SpiderRepository spiderRepository;

	@Mock
	private BlobStore pageStore;

	@Mock
	private MetadataStore metadataStore;

	@Mock
	private TimelineService timelineService;

	@Captor
	private ArgumentCaptor<Set<Link>> captor;

	@Test
	public void no_filtering() throws IOException {

		SpiderService spiderService = new SpiderService(spiderRepository, null, new ExtractorService(new ScriptingService(), new SelectorService()),
				new HttpRequester(), null, timelineService);

		Spider spider = new Spider();
		spider.setName("wikipedia");

		spider.getStores().setPageMetadataStore(metadataStore);
		spider.getStores().setPageStore(pageStore);

		spiderService.validate(spider);

		Set<String> temp = new HashSet<>();
		Mockito.when(metadataStore.filter(Mockito.anyLong(), captor.capture(), Mockito.any(Politeness.class))).thenReturn(temp);

		InputStream body = new ClassPathResource("/data/wikipedia.html").getInputStream();
		WebPage webPage = new WebPage(new URL("http://fr.wikipedia.org/wiki/Wikip%C3%A9dia"), 200, "", null, null, IOUtils.toByteArray(body));
		Analysis report = spiderService.buildReport(spider, webPage);

		Mockito.verify(metadataStore).filter(Mockito.anyLong(), Mockito.anySetOf(Link.class), Mockito.any(Politeness.class));
		temp.addAll(captor.getValue().stream().map(l -> l.getUri()).collect(Collectors.toSet()));

		Assertions.assertThat(report.getOutlinks().get(Constants._DEFAULT_OUTLINKS_EXTRATOR).size()).isGreaterThan(0);
		Assertions.assertThat(report.getFilteredOutlinks().get(Constants._DEFAULT_OUTLINKS_EXTRATOR).size()).isGreaterThan(0);

		Assertions.assertThat(report.getOutlinks().get(Constants._DEFAULT_OUTLINKS_EXTRATOR).stream().map(l -> l.getUri()).collect(Collectors.toSet()))
				.usingFieldByFieldElementComparator().isEqualTo(report.getFilteredOutlinks().get(Constants._DEFAULT_OUTLINKS_EXTRATOR));
	}

	@Test
	public void same_domain() throws IOException {

		SpiderService spiderService = new SpiderService(spiderRepository, null, new ExtractorService(new ScriptingService(), new SelectorService()),
				new HttpRequester(), null, timelineService);

		Spider spider = new Spider();
		spider.setName("wikipedia");
		AllowedForDomainsFilter filter = new AllowedForDomainsFilter();
		filter.setDomains(Arrays.asList(".wikipedia.org"));
		spider.getFilters().getForLinks().add(filter);

		spider.getStores().setPageMetadataStore(metadataStore);
		spider.getStores().setPageStore(pageStore);

		spiderService.validate(spider);

		Set<String> temp = new HashSet<>();
		Mockito.when(metadataStore.filter(Mockito.anyLong(), captor.capture(), Mockito.any(Politeness.class))).thenReturn(temp);

		InputStream body = new ClassPathResource("/data/wikipedia.html").getInputStream();
		WebPage webPage = new WebPage(new URL("http://fr.wikipedia.org/wiki/Wikip%C3%A9dia"), 200, "", null, null, IOUtils.toByteArray(body));
		Analysis report = spiderService.buildReport(spider, webPage);

		Mockito.verify(metadataStore).filter(Mockito.anyLong(), Mockito.anySetOf(Link.class), Mockito.any(Politeness.class));
		temp.addAll(captor.getValue().stream().map(l -> l.getUri()).collect(Collectors.toSet()));

		Assertions.assertThat(report.getOutlinks().get(Constants._DEFAULT_OUTLINKS_EXTRATOR).size()).isGreaterThan(0);
		Assertions.assertThat(report.getFilteredOutlinks().get(Constants._DEFAULT_OUTLINKS_EXTRATOR).size()).isGreaterThan(0);

		Assertions.assertThat(report.getOutlinks().get(Constants._DEFAULT_OUTLINKS_EXTRATOR)).usingFieldByFieldElementComparator()
				.isNotEqualTo(report.getFilteredOutlinks().get(Constants._DEFAULT_OUTLINKS_EXTRATOR));

	}
}