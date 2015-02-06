package io.mandrel.common.store.impl;

import java.util.Map;

import io.mandrel.common.WebPage;
import io.mandrel.common.store.PageMetadataStore;
import io.mandrel.common.store.WebPageStore;
import lombok.Data;

@Data
public class JdbcStore implements WebPageStore, PageMetadataStore {

	public void save(WebPage webPage) {
	}

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void init(Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}
}
