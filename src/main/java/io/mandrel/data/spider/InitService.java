package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class InitService {

	private final HazelcastInstance instance;

	public void injectAndInit(Spider spider) {
		spider.getStores().getMetadataStore().setHazelcastInstance(instance);
		if (spider.getStores().getBlobStore() != null) {
			spider.getStores().getBlobStore().setHazelcastInstance(instance);
		}
		if (spider.getExtractors() != null && spider.getExtractors().getPages() != null) {
			spider.getExtractors().getPages().forEach(ex -> ex.getDocumentStore().setHazelcastInstance(instance));
		}

		// TODO
		Map<String, Object> properties = new HashMap<>();

		spider.getStores().getMetadataStore().init(properties);
		if (spider.getStores().getBlobStore() != null) {
			spider.getStores().getBlobStore().init(properties);
		}
	}
}
