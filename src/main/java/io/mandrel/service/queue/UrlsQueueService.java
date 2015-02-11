package io.mandrel.service.queue;

import io.mandrel.requester.Requester;
import io.mandrel.service.extract.ExtractorService;
import io.mandrel.service.spider.Spider;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

@Component
public class UrlsQueueService {

	private final QueueService queueService;

	private final Requester requester;

	private final ExtractorService extractorService;

	@Inject
	public UrlsQueueService(QueueService queueService, Requester requester,
			ExtractorService extractorService) {
		this.queueService = queueService;
		this.requester = requester;
		this.extractorService = extractorService;
	}

	public void add(long spiderId, List<String> urls) {
		queueService.add("urls-" + spiderId, new EnqueuedUrls(spiderId, urls));
	}

	public void registrer(Spider spider) {
		queueService.registrer("urls-" + spider.getId(), bag -> {
			((EnqueuedUrls) bag).getUrls().forEach(url -> {
				doRequest(spider, url);
			});
		});
	}

	private void doRequest(Spider spider, String url) {
		requester.get(
				url,
				spider,
				webPage -> {
					spider.getExtractors().forEach(
							ex -> extractorService.extractFormatThenStore(
									webPage, ex));
				});
	}
}
