package io.mandrel.data.content.selector;

import io.mandrel.data.content.SourceType;

import java.util.List;

public interface Selector<X> {

	String getName();

	SourceType getSource();

	public interface Instance<T> {

		<U> List<U> select(String value, DataConverter<T, U> converter);
	}
}
