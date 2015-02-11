package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.SourceType;

import java.io.InputStream;
import java.util.List;

public interface WebPageSelector {

	String getName();

	Instance init(WebPage webpage, InputStream data);

	SourceType getSource();

	public interface Instance {

		List<String> select(String value);
	}
}
