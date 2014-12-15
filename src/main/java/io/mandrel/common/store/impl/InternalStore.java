package io.mandrel.common.store.impl;

import io.mandrel.common.WebPage;
import io.mandrel.common.store.PageMetadataStore;
import io.mandrel.common.store.WebPageStore;

import com.hazelcast.core.HazelcastInstance;

public class InternalStore implements WebPageStore, PageMetadataStore {

	private final HazelcastInstance instance;

	public InternalStore(HazelcastInstance instance) {
		this.instance = instance;
	}

	public void save(WebPage webPage) {
		instance.getMap("store").set(webPage.getUrl().toString(), webPage);
	}
}
