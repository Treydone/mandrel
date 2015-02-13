package io.mandrel.service.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.common.source.Source;
import io.mandrel.service.queue.UrlsQueueService;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

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

		while (true) {
			source.register(lst -> {
				// TODO create bag!!!!!!
				urlsQueueService.add(spider.getId(), Arrays.asList(lst));
			});
		}
	}
}