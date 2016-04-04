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

import io.mandrel.blob.BlobStores;
import io.mandrel.common.NotFoundException;
import io.mandrel.common.data.Job;
import io.mandrel.data.content.DataExtractor;
import io.mandrel.data.content.DefaultDataExtractor;
import io.mandrel.document.DocumentStore;
import io.mandrel.document.DocumentStores;
import io.mandrel.document.NavigableDocumentStore;
import io.mandrel.job.JobService;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.Optional;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ExporterService {

	private final JobService jobService;

	public void export(Long id, String extractorName, Exporter exporter, Writer writer) {
		Job job = jobService.get(id);

		Optional<? extends DataExtractor> oExtractor = job.getExtractors().getData().stream().filter(ext -> ext.getName().equals(extractorName)).findFirst();
		if (oExtractor.isPresent()) {

			DataExtractor theExtractor = oExtractor.get();
			if (!(theExtractor instanceof DefaultDataExtractor)) {
				throw new NotImplementedException("Not a default data extractor");
			}

			try {
				exporter.init(writer);

				DocumentStore theStore = DocumentStores.get(id, extractorName).get();
				if (!theStore.isNavigable()) {
					throw new NotImplementedException("Not a navigable document store");
				}
				NavigableDocumentStore store = (NavigableDocumentStore) theStore;

				store.byPages(1000, data -> {
					try {
						exporter.export(data, ((DefaultDataExtractor) theExtractor).getFields());
					} catch (Exception e) {
						log.debug("Uhhh...", e);
						return false;
					}
					return CollectionUtils.isNotEmpty(data);
				});
			} catch (Exception e) {
				log.debug("Uhhh...", e);
			} finally {
				try {
					exporter.close();
				} catch (Exception e1) {
					log.debug("Uhhh...", e1);
				}
			}
		} else {
			notFound("Extractor not found");
			log.debug("Extract {} not found for job {}", extractorName, id);
		}
	}

	public void export(Long id, Exporter exporter, Writer writer) {
		try {
			exporter.init(new BufferedWriter(writer));
			BlobStores.get(id).ifPresent(b -> b.byPages(1000, data -> {
				try {
					exporter.export(data);
				} catch (Exception e) {
					log.debug("Uhhh...", e);
					return false;
				}
				return CollectionUtils.isNotEmpty(data);
			}));
		} catch (Exception e) {
			log.debug("Uhhh...", e);
		} finally {
			try {
				exporter.close();
			} catch (Exception e1) {
				log.debug("Uhhh...", e1);
			}
		}
	}

	private void notFound(String message) {
		throw new NotFoundException(message);
	}
}
