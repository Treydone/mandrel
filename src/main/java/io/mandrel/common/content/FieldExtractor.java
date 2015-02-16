package io.mandrel.common.content;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = false)
public class FieldExtractor extends NamedDataExtractorFormatter {

	private static final long serialVersionUID = 2268103421186155100L;

	@JsonProperty("name")
	private String name;

	@JsonProperty("use_multiple")
	private boolean useMultiple = true;

	@JsonProperty("extractor")
	private Extractor extractor;

	@JsonProperty("formatter")
	private Formatter formatter;
}
