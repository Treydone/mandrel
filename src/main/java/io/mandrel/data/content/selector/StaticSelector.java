package io.mandrel.data.content.selector;

import io.mandrel.http.WebPage;

import java.util.Arrays;
import java.util.List;

public class StaticSelector extends EmptySelector<String> {

	@Override
	public String getName() {
		return "static";
	}

	public Instance<String> init(WebPage webpage) {
		return new Instance<String>() {
			@Override
			public <T> List<T> select(String value, DataConverter<String, T> converter) {
				return Arrays.asList(converter.convert(value));
			}
		};
	}
}
