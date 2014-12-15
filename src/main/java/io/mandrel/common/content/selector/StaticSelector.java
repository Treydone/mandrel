package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;

import java.util.Arrays;
import java.util.List;

public class StaticSelector implements WebPageSelector {

	@Override
	public String getName() {
		return "static";
	}

	@Override
	public Instance init(WebPage webpage) {
		return new Instance() {

			@Override
			public List<Object> select(byte[] value) {
				return Arrays.<Object> asList(new String(value));
			}
		};
	}
}
