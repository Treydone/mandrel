package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.messaging.UrlsQueueService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SpiderTask implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6204571043673228240L;

	private Spider spider;

	private transient UrlsQueueService urlsQueueService;

	@Autowired
	public void setUrlsQueueService(UrlsQueueService urlsQueueService) {
		this.urlsQueueService = urlsQueueService;
	}

	public SpiderTask(Spider spider) {
		this.spider = spider;
	}

	@Override
	public void run() {

		// TODO
		Map<String, Object> properties = new HashMap<>();

		spider.getStores().getPageMetadataStore().init(properties);
		spider.getStores().getPageStore().init(properties);

		if (spider.getExtractors().getPages() != null) {
			spider.getExtractors().getPages().stream().forEach(ex -> ex.getDataStore().init(ex));
		}

		urlsQueueService.registrer(spider);
	}
}