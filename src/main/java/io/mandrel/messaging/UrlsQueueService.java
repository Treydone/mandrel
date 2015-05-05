package io.mandrel.messaging;

import io.mandrel.common.data.Spider;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.http.Metadata;
import io.mandrel.http.Requester;
import io.mandrel.stats.Stats;
import io.mandrel.stats.StatsService;

import java.util.Set;

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
		queueService.add("urls-" + spiderId, urls);
	}

	public void registrer(Spider spider) {
		log.debug("Registering spider {} ({})", spider.getName(), spider.getId());

		Stats stats = statsService.get(spider.getId());
		queueService.<String> registrer("urls-" + spider.getId(), url -> {
			long maxPages = spider.getClient().getPoliteness().getMaxPages();
			if (maxPages > 0 && stats.getNbPages() > maxPages) {
				log.debug("Max pages reached for {} ({})", spider.getName(), spider.getId());
				return true;
			}
			doRequest(spider, url, stats);
			return false;
		});
	}

	private void doRequest(Spider spider, String url, Stats stats) {
		try {
			StopWatch watch = new StopWatch();
			watch.start();

			// Mark as pending
			queueService.markAsPending("pendings-" + spider.getId(), url, Boolean.TRUE);

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
							Set<String> allFilteredOutlinks = extractorService.extractAndFilterOutlinks(spider, url, webPage, ol).getRight();

							metadata.setOutlinks(allFilteredOutlinks);

							// Respect politeness for this
							// spider
							// TODO

								// Add outlinks to queue if they are not already
								// present
								allFilteredOutlinks = queueService.deduplicate("urls-" + spider.getId(), allFilteredOutlinks);
								
								allFilteredOutlinks = queueService.filterPendings("pendings-" + spider.getId(), allFilteredOutlinks);
								add(spider.getId(), allFilteredOutlinks);
							});
					}

					if (spider.getStores().getPageStore() != null) {
						spider.getStores().getPageStore().addPage(spider.getId(), webPage.getUrl().toString(), webPage);
					}
					spider.getStores().getPageMetadataStore().addMetadata(spider.getId(), webPage.getUrl().toString(), metadata);
					queueService.removePending("pendings-" + spider.getId(), url);
				});
		} catch (Exception e) {
			log.debug("Can not fetch url {} due to {}", new Object[] { url, e.toString() }, e);
		}
	}

}
