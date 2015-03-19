package io.mandrel.data.content.selector;

import io.mandrel.data.content.SourceType;

import java.util.List;

public interface Selector {

	String getName();

	SourceType getSource();

	public interface Instance {

		List<String> select(String value);
	}
}
