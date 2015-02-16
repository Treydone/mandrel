package io.mandrel.common.content;

import io.mandrel.common.filters.WebPageFilter;
import io.mandrel.common.store.DocumentStore;
import io.mandrel.common.store.impl.InternalDocumentStore;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class WebPageExtractor implements Serializable {

	private static final long serialVersionUID = -4537707331477217731L;

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
