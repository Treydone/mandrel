package io.mandrel.gateway.impl;

import io.mandrel.common.data.Politeness;
import io.mandrel.gateway.PageMetadataStore;
import io.mandrel.gateway.WebPageStore;
import io.mandrel.http.WebPage;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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

	public void addPage(long spiderId, WebPage webPage) {

	}

	@Override
	public void addMetadata(long spiderId, WebPage webPage) {
		// TODO Auto-generated method stub

	}

	@Override
	public Stream<WebPage> all(long spiderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> filter(long spiderId, Set<String> outlinks, Politeness politeness) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAllFor(long spiderId) {
		// TODO Auto-generated method stub

	}
}
