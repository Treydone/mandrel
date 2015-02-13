package io.mandrel.common.content.selector;

import io.mandrel.common.content.SourceType;

import java.util.List;

public interface Selector {

	String getName();

	SourceType getSource();

	public interface Instance {

		List<String> select(String value);
	}
}
