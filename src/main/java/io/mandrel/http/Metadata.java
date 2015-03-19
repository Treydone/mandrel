package io.mandrel.http;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;

@Data
public class Metadata implements Serializable {

	private static final long serialVersionUID = -7319512873380929621L;

	private final int statusCode;
	private final String statusText;
	private final FluentCaseInsensitiveStringsMap headers;
	private final List<Cookie> cookies;
	private final LocalDateTime lastCrawlDate;

	public Metadata(int statusCode, String statusText, FluentCaseInsensitiveStringsMap headers, List<Cookie> cookies) {
		super();
		this.statusCode = statusCode;
		this.statusText = statusText;
		this.headers = headers;
		this.cookies = cookies;
		this.lastCrawlDate = LocalDateTime.now();
	}
}