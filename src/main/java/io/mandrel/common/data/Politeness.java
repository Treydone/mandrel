package io.mandrel.common.data;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Politeness implements Serializable {

	private static final long serialVersionUID = -3487435400772289245L;

	@JsonProperty("global_rate")
	private long globalRate = 1000;

	@JsonProperty("per_node_rate")
	private long perNodeRate = 500;

	@JsonProperty("max_pages")
	private long maxPages = 500;

	@JsonProperty("wait")
	private long wait = 100;

	@JsonProperty("ignore_robots_txt")
	private boolean ignoreRobotsTxt = false;

	@JsonProperty("recrawl_after")
	private int recrawlAfterSeconds = -1;

}
