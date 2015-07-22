package io.mandrel.due;

import java.util.Collection;
import java.util.Set;

public interface DuplicateUrlEliminator {

	<T> void markAsPending(String queueName, String identifier, T data);

	<T> void removePending(String queueName, String identifier);

	<T> Set<T> filterPendings(String queueName, Collection<T> identifiers);

	<T> void add(String queueName, T data);

	<T> void add(String queueName, Collection<T> data);

	<T> void remove(String queueName, Collection<T> data);

	<T> Set<T> deduplicate(String queueName, Collection<T> data);
}
