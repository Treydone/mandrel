package io.mandrel.data.export;

import io.mandrel.common.data.Spider;
import io.mandrel.data.content.WebPageExtractor;
import io.mandrel.data.spider.SpiderService;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@Slf4j
public class ExporterService {

	private final SpiderService spiderService;

	@Autowired
	public ExporterService(SpiderService spiderService) {
		super();
		this.spiderService = spiderService;
	}

	public void export(Long id, String extractorName, Exporter exporter, HttpServletResponse response) {
		Optional<Spider> spider = spiderService.get(id);

		if (spider.isPresent()) {
			Optional<WebPageExtractor> extractor = spider.get().getExtractors().getPages().stream()
					.filter(ext -> ext.getName().equals(extractorName)).findFirst();
			if (extractor.isPresent()) {
				response.setContentType(exporter.contentType());
				try {
					exporter.export(extractor.get().getDataStore().all(), extractor.get().getFields(), response.getWriter());
				} catch (Exception e) {
					log.debug("Uhhh...", e);
				}
			} else {
				response.setStatus(HttpStatus.NOT_FOUND.value());
				log.debug("Extract {} not found for spider {}", extractorName, id);
			}
		} else {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			log.debug("Spider {} not found", id);
		}

	}
}
