package io.mandrel.gateway.impl;

import io.mandrel.common.data.Politeness;
import io.mandrel.data.spider.Link;
import io.mandrel.gateway.PageMetadataStore;
import io.mandrel.gateway.WebPageStore;
import io.mandrel.http.Metadata;
import io.mandrel.http.WebPage;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import lombok.Data;

@Data
public class JdbcStore implements WebPageStore, PageMetadataStore {

	private static final long serialVersionUID = -4148862105449045170L;

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void init(Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	public void addPage(long spiderId, String url, WebPage webPage) {
	}

	@Override
	public void addMetadata(long spiderId, String url, Metadata metadata) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<Link> filter(long spiderId, Set<Link> outlinks, Politeness politeness) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<WebPage> all(long spiderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAllFor(long spiderId) {
		// TODO Auto-generated method stub

	}
}
