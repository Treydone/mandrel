package io.mandrel.data.content.selector;

import io.mandrel.http.WebPage;

import java.util.Arrays;
import java.util.List;

public class StaticSelector extends EmptySelector {

	@Override
	public String getName() {
		return "static";
	}

	public Instance init(WebPage webpage) {
		return new Instance() {

			@Override
			public List<String> select(String value) {
				return Arrays.asList(value);
			}
		};
	}
}
