package io.mandrel.messaging;

import io.mandrel.common.data.Spider;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.data.spider.Link;
import io.mandrel.http.Requester;

import java.util.Set;
import java.util.stream.Collectors;

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

	public void add(long spiderId, Set<String> urls) {
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

			spider.getStores().getPageStore().addPage(spider.getId(), webPage);
			spider.getStores().getPageMetadataStore().addMetadata(spider.getId(), webPage);

			if (spider.getExtractors() != null) {
				spider.getExtractors().getPages().forEach(ex -> extractorService.extractThenFormatThenStore(spider.getId(), webPage, ex));
			}
			if (spider.getExtractors().getOutlinks() != null) {
				spider.getExtractors().getOutlinks().forEach(ol -> {
					// Find outlinks in page
						Set<Link> outlinks = extractorService.extractOutlinks(webPage, ol);

						// Filter outlinks
						outlinks = spider.getStores().getPageMetadataStore().filter(spider.getId(), outlinks, spider.getClient().getPoliteness());

						// Respect politeness for this spider
						// spider.getClient().getPoliteness().getMaxPages();

						// Add outlinks to queue
						add(spider.getId(), outlinks.stream().map(l -> l.getUrl()).collect(Collectors.toSet()));
					});
			}
		});
	}
}
