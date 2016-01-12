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
import io.mandrel.common.data.Spider;
import io.mandrel.data.content.MetadataExtractor;
import io.mandrel.document.DocumentStores;
import io.mandrel.spider.SpiderService;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.Optional;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ExporterService {

	private final SpiderService spiderService;

	public void export(Long id, String extractorName, Exporter exporter, Writer writer) {
		Spider spider = spiderService.get(id);

		Optional<MetadataExtractor> oExtractor = spider.getExtractors().getPages().stream().filter(ext -> ext.getName().equals(extractorName)).findFirst();
		if (oExtractor.isPresent()) {
			try {
				exporter.init(writer);
				MetadataExtractor extractor = oExtractor.get();
				DocumentStores.get(id, extractorName).get().byPages(1000, data -> {
					try {
						exporter.export(data, extractor.getFields());
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
			log.debug("Extract {} not found for spider {}", extractorName, id);
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
