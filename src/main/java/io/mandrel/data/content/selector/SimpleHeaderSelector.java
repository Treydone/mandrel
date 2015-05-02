package io.mandrel.data.content.selector;

import io.mandrel.http.WebPage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleHeaderSelector extends HeaderSelector<String> {

	public Instance<String> init(WebPage webpage, Map<String, List<String>> headers) {
		return new Instance<String>() {
			@Override
			public <T> List<T> select(String value, DataConverter<String, T> converter) {
				return headers.get(value) != null ? headers.get(value).stream().map(converter::convert).collect(Collectors.toList()) : null;
			}
		};
	}

	@Override
	public String getName() {
		return "header";
	}
}
