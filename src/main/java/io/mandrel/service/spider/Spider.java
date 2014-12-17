package io.mandrel.service.spider;

import io.mandrel.common.filters.WebPageFilter;
import io.mandrel.common.source.Source;
import io.mandrel.common.store.DataStore;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class Spider {
	private int requestTimeOut;
	private Map<String, Collection<String>> headers;
	private Map<String, List<String>> params;

	private List<Source> sources;
	private List<WebPageFilter> filters;

	private Map<String, DataStore> stores;
}
