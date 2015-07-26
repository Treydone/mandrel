package io.mandrel.due;

import java.util.Collection;
import java.util.Set;

public interface DuplicateUrlEliminator {

	<T> void markAsPending(String queueName, String identifier, T data);

	<T> void removePending(String queueName, String identifier);

	<T> Set<T> filterPendings(String queueName, Collection<T> identifiers);

	<T> Set<T> deduplicate(String queueName, Collection<T> data);
}
