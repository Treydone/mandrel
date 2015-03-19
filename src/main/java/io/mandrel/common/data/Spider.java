package io.mandrel.common.data;

import io.mandrel.data.filters.WebPageFilter;
import io.mandrel.data.source.Source;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Spider implements Serializable {

	private static final long serialVersionUID = 7577967853566572778L;

	@JsonProperty("id")
	private long id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("status")
	private State state = State.NEW;

	@JsonProperty("sources")
	@NotNull
	private List<Source> sources;

	@JsonProperty("filter")
	private List<WebPageFilter> filters;

	@JsonProperty("extractors")
	private Extractors extractors = new Extractors();

	@JsonProperty("stores")
	private Stores stores = new Stores();

	@JsonProperty("client")
	private Client client = new Client();
}
