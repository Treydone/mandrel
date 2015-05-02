package io.mandrel.messaging;

import io.mandrel.common.data.Spider;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.data.spider.Link;
import io.mandrel.http.Metadata;
import io.mandrel.http.Requester;
import io.mandrel.stats.Stats;
import io.mandrel.stats.StatsService;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Slf4j
public class UrlsQueueService {

	private final QueueService queueService;

	private final Requester requester;

	private final StatsService statsService;

	private final ExtractorService extractorService;

	@Inject
	public UrlsQueueService(QueueService queueService, Requester requester, ExtractorService extractorService, StatsService statsService) {
		this.queueService = queueService;
		this.requester = requester;
		this.extractorService = extractorService;
		this.statsService = statsService;
	}

	public void add(long spiderId, Set<String> urls) {
		queueService.add("urls-" + spiderId, new EnqueuedUrls(spiderId, urls));
	}

	public void registrer(Spider spider) {
		log.debug("Registering spider {} ({})", spider.getName(), spider.getId());

		Stats stats = statsService.get(spider.getId());
		queueService.registrer("urls-" + spider.getId(), data -> {
			EnqueuedUrls bag = (EnqueuedUrls) data;
			return bag.getUrls().stream().anyMatch(url -> {
				long maxPages = spider.getClient().getPoliteness().getMaxPages();
				if (maxPages > 0 && stats.getNbPages() > maxPages) {
					log.debug("Max pages reached for {} ({})", spider.getName(), spider.getId());
					return true;
				}
				doRequest(spider, url, stats);
				return false;
			});
		});
	}

	private void doRequest(Spider spider, String url, Stats stats) {
		log.trace("Requesting {}...", url);

		try {
			StopWatch watch = new StopWatch();
			watch.start();

			requester.get(url, spider, webPage -> {
				watch.stop();
				log.trace("Getting response for {}", url);

				Metadata metadata = webPage.getMetadata();
				metadata.setTimeToFetch(watch.getTotalTimeMillis());

				stats.incNbPages();
				stats.incPageForStatus(metadata.getStatusCode());
				// TODO
				// stats.incTotalSize(size);

					if (spider.getExtractors() != null && spider.getExtractors().getPages() != null) {
						spider.getExtractors().getPages().forEach(ex -> extractorService.extractThenFormatThenStore(spider.getId(), webPage, ex));
					}
					if (spider.getExtractors().getOutlinks() != null) {
						spider.getExtractors().getOutlinks().forEach(ol -> {
							// Find outlinks in page
								Set<Link> outlinks = extractorService.extractOutlinks(webPage, ol);

								// Filter outlinks
								outlinks = spider.getStores().getPageMetadataStore().filter(spider.getId(), outlinks, spider.getClient().getPoliteness());

								metadata.setOutlinks(outlinks);

								// Respect politeness for this spider
								// spider.getClient().getPoliteness().getMaxPages();

								// Add outlinks to queue
								add(spider.getId(), outlinks.stream().map(l -> l.getUri()).collect(Collectors.toSet()));
							});
					}

					if (spider.getStores().getPageStore() != null) {
						spider.getStores().getPageStore().addPage(spider.getId(), webPage.getUrl().toString(), webPage);
					}
					spider.getStores().getPageMetadataStore().addMetadata(spider.getId(), webPage.getUrl().toString(), metadata);
				});
		} catch (Exception e) {
			log.info("Hum...", url);
		}
	}
}
