package io.mandrel.common.content;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Field {

	@JsonProperty("name")
	private String name;
	
	@JsonProperty("use_multiple")
	private boolean useMultiple = true;

	@JsonProperty("extractor")
	private Extractor extractor;

	@JsonProperty("formatter")
	private Formatter formatter;
}
