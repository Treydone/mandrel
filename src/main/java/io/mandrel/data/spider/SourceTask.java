package io.mandrel.data.spider;

import io.mandrel.data.source.Source;
import io.mandrel.messaging.UrlsQueueService;

import java.io.Serializable;
import java.util.Map;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
@Setter
public class SourceTask implements Runnable, Serializable {

	private static final long serialVersionUID = -6204571043673228240L;

	private long spiderId;
	private Source source;

	@Autowired
	private transient UrlsQueueService urlsQueueService;

	public SourceTask(long spiderId, Source source) {
		this.spiderId = spiderId;
		this.source = source;
	}

	@Override
	public void run() {
		// TODO
		Map<String, Object> properties = null;

		source.init(properties);

		source.register(lst -> {
			urlsQueueService.add(spiderId, Sets.newHashSet(lst));
		});
	}
}