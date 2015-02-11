package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.SourceType;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class StaticSelector implements WebPageSelector {

	@Override
	public String getName() {
		return "static";
	}

	@Override
	public SourceType getSource() {
		return SourceType.EMPTY;
	}

	@Override
	public Instance init(WebPage webpage, InputStream data) {
		return new Instance() {

			@Override
			public List<String> select(String value) {
				return Arrays.asList(value);
			}
		};
	}
}
