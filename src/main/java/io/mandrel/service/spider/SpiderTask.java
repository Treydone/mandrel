package io.mandrel.service.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.service.queue.UrlsQueueService;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SpiderTask implements Runnable, Serializable, HazelcastInstanceAware, ApplicationContextAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6204571043673228240L;

	private Spider spider;

	private transient ApplicationContext context;

	private transient HazelcastInstance instance;

	public void setHazelcastInstance(HazelcastInstance instance) {
		this.instance = instance;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	public SpiderTask(Spider spider) {
		this.spider = spider;
	}

	@Autowired
	private transient UrlsQueueService urlsQueueService;

	@Override
	public void run() {

		// TODO
		Map<String, Object> properties = null;

		spider.getStores().getPageMetadataStore().init(properties);
		spider.getStores().getPageStore().init(properties);

		if (spider.getExtractors().getPages() != null) {
			spider.getExtractors().getPages().stream().forEach(ex -> ex.getDataStore().init(ex));
		}

		urlsQueueService.registrer(spider);

	}
}