package io.mandrel.common.store.impl;

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
}
