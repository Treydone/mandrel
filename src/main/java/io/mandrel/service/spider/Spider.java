package io.mandrel.service.spider;

import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.filters.WebPageFilter;
import io.mandrel.common.source.Source;
import io.mandrel.common.store.DocumentStore;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
public class Spider {
	private long id;
	private State state = State.NEW;
	private int requestTimeOut;
	private Map<String, Collection<String>> headers;
	private Map<String, List<String>> params;

	@NotNull
	private List<Source> sources;
	private List<WebPageFilter> filters;
	private List<WebPageExtractor> extractors;

	private Map<String, DocumentStore> stores;

	@JsonIgnore
	public void setState(State state) {
		this.state = state;
	}

}
