package io.mandrel.common.content;

import io.mandrel.common.filters.WebPageFilter;
import io.mandrel.common.store.DocumentStore;
import io.mandrel.common.store.impl.InternalDocumentStore;

import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class WebPageExtractor {

	@JsonProperty("name")
	private String name;

	@JsonProperty("store")
	private DocumentStore dataStore = new InternalDocumentStore();

	@JsonProperty("filters")
	private List<WebPageFilter> filters;

	@JsonProperty("multiple")
	private Extractor multiple;

	@JsonProperty("fields")
	private List<FieldExtractor> fields;

}
