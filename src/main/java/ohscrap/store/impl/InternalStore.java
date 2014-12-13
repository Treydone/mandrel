package ohscrap.store.impl;

import com.hazelcast.core.HazelcastInstance;

import ohscrap.common.WebPage;
import ohscrap.store.PageMetadataStore;
import ohscrap.store.WebPageStore;

public class InternalStore implements WebPageStore, PageMetadataStore {

	private final HazelcastInstance instance;

	public InternalStore(HazelcastInstance instance) {
		this.instance = instance;
	}

	public void save(WebPage webPage) {
		instance.getMap("store").set(webPage.getUrl().toString(), webPage);
	}
}
