package io.mandrel.data.content.selector;

import io.mandrel.http.WebPage;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class AllUrlSelector extends UrlSelector<String> {

	public Instance<String> init(WebPage webpage, URL url) {
		return new Instance<String>() {
			@Override
			public <T> List<T> select(String value, DataConverter<String, T> converter) {
				return Arrays.asList(converter.convert(url.toString()));
			}
		};
	}

	@Override
	public String getName() {
		return "full_url";
	}
}
