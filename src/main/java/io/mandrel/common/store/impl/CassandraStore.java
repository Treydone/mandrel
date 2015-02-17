package io.mandrel.common.store.impl;

import io.mandrel.common.WebPage;
import io.mandrel.common.data.Politeness;
import io.mandrel.common.store.PageMetadataStore;
import io.mandrel.common.store.WebPageStore;

import java.util.Map;
import java.util.Set;

public class CassandraStore implements WebPageStore, PageMetadataStore {

	private static final long serialVersionUID = 6800608875261746768L;

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void init(Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	public void addPage(WebPage webPage) {

	}

	@Override
	public void addMetadata(WebPage webPage) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> filter(Set<String> outlinks, Politeness politeness) {
		// TODO Auto-generated method stub
		return null;
	}
}
