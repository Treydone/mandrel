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
package io.mandrel.data.export;

import io.mandrel.common.data.Spider;
import io.mandrel.data.spider.SpiderService;
import io.mandrel.gateway.BlobStore;
import io.mandrel.gateway.BlobStore.Callback;
import io.mandrel.requests.WebPage;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExporterServiceTest {

	@Mock
	private SpiderService spiderService;

	@Mock
	private Writer writer;

	@Mock
	private BlobStore store;

	@Captor
	private ArgumentCaptor<Callback> captor;

	private ExporterService service;

	@Before
	public void before() {
		service = new ExporterService(spiderService);
	}

	@Test
	public void raw_export() {

		// Arrange
		List<WebPage> results = new ArrayList<>();

		RawExporter exporter = new RawExporter() {
			public String contentType() {
				return null;
			}

			public void export(Collection<WebPage> documents) {
				results.addAll(documents);
			}

			public void init(Writer writer) {
			}

			public void close() {
			}

			@Override
			public String getType() {
				return null;
			}
		};

		Spider spider = new Spider();
		spider.getStores().setPageStore(store);

		Mockito.when(spiderService.get(0)).thenReturn(Optional.of(spider));
		// Mockito.when(store.byPages(0L, 1000, captor.capture()));

		// Actions
		service.export(0L, exporter, writer);

		// Asserts
		Assertions.assertThat(results).hasSize(2);
	}
}
