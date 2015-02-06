package io.mandrel.common.store.impl;

import io.mandrel.common.WebPage;
import io.mandrel.common.store.PageMetadataStore;
import io.mandrel.common.store.WebPageStore;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.core.HazelcastInstance;

@Data
public class InternalStore implements WebPageStore, PageMetadataStore {

	@JsonIgnore
	private HazelcastInstance instance;

	public InternalStore() {
	}

	public void save(WebPage webPage) {
		instance.getMap("store").set(webPage.getUrl().toString(), webPage);
	}

	@Override
	public boolean check() {
		return true;
	}
}
