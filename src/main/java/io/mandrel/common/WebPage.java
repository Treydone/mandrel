package io.mandrel.common;

import java.io.InputStream;
import java.net.URL;

public class WebPage {
	
	private URL url;

	private InputStream dataStream;

	public WebPage(URL url, InputStream dataStream) {
		super();
		this.url = url;
		this.dataStream = dataStream;
	}

	public URL getUrl() {
		return url;
	}

	public InputStream getDataStream() {
		return dataStream;
	}
}
