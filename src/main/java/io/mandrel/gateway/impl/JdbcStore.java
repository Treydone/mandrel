package io.mandrel.gateway.impl;

import io.mandrel.common.data.Politeness;
import io.mandrel.gateway.PageMetadataStore;
import io.mandrel.gateway.WebPageStore;
import io.mandrel.http.WebPage;

import java.util.Map;
import java.util.Set;

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
