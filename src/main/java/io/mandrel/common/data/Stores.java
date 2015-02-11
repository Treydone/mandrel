package io.mandrel.common.data;

import io.mandrel.common.store.PageMetadataStore;
import io.mandrel.common.store.WebPageStore;
import io.mandrel.common.store.impl.InternalStore;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Stores {

	@JsonProperty("metadata")
	private PageMetadataStore pageMetadataStore = new InternalStore();

	@JsonProperty("page")
	private WebPageStore pageStore = new InternalStore();
}