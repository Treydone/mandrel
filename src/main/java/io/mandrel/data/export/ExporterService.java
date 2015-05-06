package io.mandrel.data.export;

import io.mandrel.common.NotFoundException;
import io.mandrel.common.data.Spider;
import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.data.spider.SpiderService;

import java.io.Writer;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExporterService {

	private final SpiderService spiderService;

	@Autowired
	public ExporterService(SpiderService spiderService) {
		super();
		this.spiderService = spiderService;
	}

	public void export(Long id, String extractorName, DocumentExporter exporter, Writer writer) {
		Optional<Spider> optional = spiderService.get(id);

		if (optional.isPresent()) {
			Spider spider = optional.get();
			spiderService.injectAndInit(spider);
			Optional<WebPageExtractor> extractor = spider.getExtractors().getPages().stream().filter(ext -> ext.getName().equals(extractorName)).findFirst();
			if (extractor.isPresent()) {
				try {
					exporter.init(writer);
					extractor.get().getDataStore().byPages(id, 1000, data -> {
						try {
							exporter.export(data, extractor.get().getFields());
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
		} else {
			notFound("Spider not found");
			log.debug("Spider {} not found", id);
		}
	}

	public void export(Long id, RawExporter exporter, Writer writer) {
		Optional<Spider> optional = spiderService.get(id);

		if (optional.isPresent()) {
			Spider spider = optional.get();
			spiderService.injectAndInit(spider);

			try {
				exporter.init(writer);
				spider.getStores().getPageStore().byPages(id, 1000, data -> {
					try {
						exporter.export(data);
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
			notFound("Spider not found");
			log.debug("Spider {} not found", id);
		}

	}

	private void notFound(String message) {
		throw new NotFoundException(message);
	}
}
