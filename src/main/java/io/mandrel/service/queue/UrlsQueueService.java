package io.mandrel.service.queue;

import io.mandrel.common.data.Spider;
import io.mandrel.requester.Requester;
import io.mandrel.service.extract.ExtractorService;

import java.util.List;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UrlsQueueService {

	private final QueueService queueService;

	private final Requester requester;

	private final ExtractorService extractorService;

	@Inject
	public UrlsQueueService(QueueService queueService, Requester requester, ExtractorService extractorService) {
		this.queueService = queueService;
		this.requester = requester;
		this.extractorService = extractorService;
	}

	public void add(long spiderId, List<String> urls) {
		queueService.add("urls-" + spiderId, new EnqueuedUrls(spiderId, urls));
	}

	public void registrer(Spider spider) {
		log.debug("Registering spider {} ({})", spider.getName(), spider.getId());
		queueService.registrer("urls-" + spider.getId(), data -> {
			EnqueuedUrls bag = (EnqueuedUrls) data;
			bag.getUrls().forEach(url -> {
				doRequest(spider, url);
			});
		});
	}

	private void doRequest(Spider spider, String url) {
		log.trace("Requesting {}...", url);
		requester.get(url, spider, webPage -> {
			log.trace("Getting response for {}", url);
			if (spider.getExtractors() != null) {
				spider.getExtractors().getPages().forEach(ex -> extractorService.extractFormatThenStore(webPage, ex));
			}
			if (spider.getExtractors().getOutlinks() != null) {
				spider.getExtractors().getOutlinks().forEach(ol -> add(spider.getId(), extractorService.extractOutlinks(webPage, ol)));
			}
		});
	}
}
