package io.mandrel.common.data;

import java.io.Serializable;

import io.mandrel.gateway.PageMetadataStore;
import io.mandrel.gateway.WebPageStore;
import io.mandrel.gateway.impl.InternalStore;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Stores implements Serializable {

	private static final long serialVersionUID = -6386148207535019331L;

	@JsonProperty("metadata")
	private PageMetadataStore pageMetadataStore = new InternalStore();

	@JsonProperty("page")
	private WebPageStore pageStore = new InternalStore();
}