package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;

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
