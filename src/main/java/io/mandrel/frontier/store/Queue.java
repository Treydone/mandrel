package io.mandrel.frontier.store;

public interface Queue<T> {

	T pool();

	void schedule(T uri);
}
