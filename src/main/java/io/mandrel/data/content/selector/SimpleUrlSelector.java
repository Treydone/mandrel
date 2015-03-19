package io.mandrel.data.content.selector;

import io.mandrel.http.WebPage;

import java.net.URL;
import java.util.List;

public class SimpleUrlSelector extends UrlSelector {

	public Instance init(WebPage webpage, URL url) {
		return new Instance() {

			@Override
			public List<String> select(String value) {
				return null;
			}
		};
	}

	@Override
	public String getName() {
		return "url";
	}
}
