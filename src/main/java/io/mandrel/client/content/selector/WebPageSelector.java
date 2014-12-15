package io.mandrel.client.content.selector;

import io.mandrel.common.WebPage;

import java.util.List;

public interface WebPageSelector {

	String getName();

	Instance init(WebPage webpage);

	public interface Instance {

		List<Object> select(byte[] value);
	}
}
