package io.mandrel.frontier;

public interface PoolCallback<T> {

	void on(T t);
}
