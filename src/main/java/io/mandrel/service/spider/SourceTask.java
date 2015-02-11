package io.mandrel.service.spider;

import io.mandrel.common.source.Source;
import io.mandrel.service.queue.UrlsQueueService;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SourceTask implements Runnable, Serializable,
		HazelcastInstanceAware, ApplicationContextAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6204571043673228240L;

	private Spider spider;

	private Source source;

	private transient ApplicationContext context;

	private transient HazelcastInstance instance;

	public void setHazelcastInstance(HazelcastInstance instance) {
		this.instance = instance;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
	}

	public SourceTask(Spider spider, Source source) {
		this.spider = spider;
		this.source = source;
	}

	@Autowired
	private transient UrlsQueueService urlsQueueService;

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