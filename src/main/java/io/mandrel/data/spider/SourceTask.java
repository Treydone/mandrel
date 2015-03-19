package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.data.source.Source;
import io.mandrel.messaging.UrlsQueueService;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SourceTask implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6204571043673228240L;

	private Spider spider;

	private Source source;

	private transient UrlsQueueService urlsQueueService;

	public SourceTask(Spider spider, Source source) {
		this.spider = spider;
		this.source = source;
	}

	@Autowired
	public void setUrlsQueueService(UrlsQueueService urlsQueueService) {
		this.urlsQueueService = urlsQueueService;
	}

	@Override
	public void run() {
		// TODO
		Map<String, Object> properties = null;

		source.init(properties);

		source.register(lst -> {
			// TODO create bag!!!!!!
			urlsQueueService.add(spider.getId(), Sets.newHashSet(lst));
		});
	}
}