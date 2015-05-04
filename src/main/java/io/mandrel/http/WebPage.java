package io.mandrel.http;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WebPage {

	private URL url;

	private Metadata metadata;

	private byte[] body;

	public WebPage(URL url, int statusCode, String statusText, Map<String, List<String>> headers, List<Cookie> cookies, byte[] body) {
		super();
		this.url = url;
		this.metadata = new Metadata();
		metadata.setCookies(cookies);
		metadata.setHeaders(headers);
		metadata.setLastCrawlDate(LocalDateTime.now());
		metadata.setStatusCode(statusCode);
		metadata.setStatusText(statusText);
		metadata.setUrl(url);

		this.body = body;
	}
}
