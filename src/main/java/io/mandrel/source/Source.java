package io.mandrel.source;

public interface Source {

	void register(EntryListener listener);
	
	String getType();
}
