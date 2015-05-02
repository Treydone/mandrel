package io.mandrel.http;

import io.mandrel.data.spider.Link;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class Metadata implements Serializable {

	private static final long serialVersionUID = -7319512873380929621L;

	private URL url;
	private int statusCode;
	private String statusText;
	private Map<String, List<String>> headers;
	private List<Cookie> cookies;
	private LocalDateTime lastCrawlDate;
	private long timeToFetch;
	private Set<Link> outlinks;
}