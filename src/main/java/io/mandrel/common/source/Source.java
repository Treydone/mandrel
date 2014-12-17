package io.mandrel.common.source;

import java.util.Map;

public interface Source {

	void init(Map<String, Object> properties);

	void register(EntryListener listener);

	String getType();
}
