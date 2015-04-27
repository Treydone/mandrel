package io.mandrel.data.content.selector;

import io.mandrel.http.WebPage;

import java.net.URL;
import java.util.List;

public class SimpleUrlSelector extends UrlSelector<String> {

	public Instance<String> init(WebPage webpage, URL url) {
		return new Instance<String>() {
			@Override
			public <T> List<T> select(String value, DataConverter<String, T> converter) {
				return null;
			}
		};
	}

	@Override
	public String getName() {
		return "url";
	}
}
