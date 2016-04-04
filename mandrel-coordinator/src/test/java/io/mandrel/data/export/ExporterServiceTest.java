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

import io.mandrel.blob.Blob;
import io.mandrel.blob.BlobStore;
import io.mandrel.blob.BlobStore.Callback;
import io.mandrel.blob.BlobStores;
import io.mandrel.common.data.Job;
import io.mandrel.data.content.FieldExtractor;
import io.mandrel.document.Document;
import io.mandrel.job.JobService;

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
	private JobService jobService;

	@Mock
	private Writer writer;

	@Mock
	private BlobStore store;

	@Captor
	private ArgumentCaptor<Callback> captor;

	private ExporterService service;

	@Before
	public void before() {
		service = new ExporterService(jobService);
	}

	@Test
	public void raw_export() {

		// Arrange
		List<Blob> results = new ArrayList<>();

		Exporter exporter = new Exporter() {
			public String contentType() {
				return null;
			}

			public void init(Writer writer) {
			}

			public void close() {
			}

			@Override
			public void export(Collection<Document> documents, List<FieldExtractor> fields) throws Exception {

			}

			@Override
			public void export(Collection<Blob> blobs) throws Exception {

			}

		};

		BlobStores.add(0, store);

		Mockito.when(jobService.get(0)).thenReturn(new Job());
		// Mockito.when(store.byPages(0L, 1000, captor.capture()));

		// Actions
		service.export(0L, exporter, writer);

		// Asserts
		Assertions.assertThat(results).hasSize(2);
	}
}
