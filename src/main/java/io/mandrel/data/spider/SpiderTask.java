package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.messaging.UrlsQueueService;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;

import com.hazelcast.spring.context.SpringAware;

@SpringAware
@Setter
public class SpiderTask implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6204571043673228240L;

	private Spider spider;

	@Autowired
	private transient UrlsQueueService urlsQueueService;

	@Autowired
	private transient SpiderService spiderService;

	public SpiderTask(Spider spider) {
		this.spider = spider;
	}

	@Override
	public void run() {

		if (spider.getExtractors().getPages() != null) {
			spider.getExtractors().getPages().stream().forEach(ex -> ex.getDataStore().init(ex));
		}

		// Block until the end
		urlsQueueService.registrer(spider);

		// End the spider on all members
		spiderService.end(spider.getId());
	}
}