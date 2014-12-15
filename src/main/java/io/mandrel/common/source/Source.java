package io.mandrel.common.source;

public interface Source {

	void register(EntryListener listener);
	
	String getType();
}
