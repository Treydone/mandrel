package io.mandrel.common.content;

import io.mandrel.common.store.DocumentStore;
import io.mandrel.common.store.impl.InternalDocumentStore;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class WebPageExtractor {

	private String name;

	@JsonProperty("store")
	private DocumentStore dataStore = new InternalDocumentStore();

	@JsonProperty("matching_patterns")
	private List<String> matchingPatternsAsString;

	@JsonIgnore
	private List<Pattern> matchingPatterns;

	@JsonProperty("fields")
	private List<Field> fields;

	@JsonProperty("outlinks")
	private List<Outlink> outlinks = Arrays.asList(new Outlink());

	public void setMatchingPatternsAsString(
			List<String> matchingPatternsAsString) {
		this.matchingPatternsAsString = matchingPatternsAsString;
		matchingPatterns = this.matchingPatternsAsString.stream()
				.map(pattern -> Pattern.compile(pattern))
				.collect(Collectors.toList());
	}

}
