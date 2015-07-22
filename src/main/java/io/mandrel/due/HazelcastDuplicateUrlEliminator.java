package io.mandrel.due;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HazelcastDuplicateUrlEliminator implements DuplicateUrlEliminator {

	private final HazelcastInstance instance;

	public <T> void markAsPending(String queueName, String identifier, T data) {
		if (identifier != null) {
			prepareIfNotDefined(queueName);

			IMap<String, T> pendings = instance.getMap(queueName);
			pendings.put(identifier, data);
		}
	}

	public <T> void removePending(String queueName, String identifier) {
		if (identifier != null) {
			prepareIfNotDefined(queueName);

			IMap<String, T> pendings = instance.getMap(queueName);
			pendings.remove(identifier);
		}
	}

	public <T> Set<T> filterPendings(String queueName, Collection<T> identifiers) {
		if (identifiers != null) {
			prepareIfNotDefined(queueName);

			IMap<String, T> pendings = instance.getMap(queueName);
			return identifiers.stream().filter(el -> !pendings.containsKey(el)).collect(Collectors.toSet());
		}
		return null;
	}

	protected void prepareIfNotDefined(String queueName) {
		if (instance.getConfig().getQueueConfigs().containsKey(queueName)) {
			// Create map of pendings with TTL of 10 secs
			MapConfig mapConfig = new MapConfig();
			mapConfig.setName(queueName).setBackupCount(1).setTimeToLiveSeconds(10).setStatisticsEnabled(true);
			instance.getConfig().addMapConfig(mapConfig);
		}
	}

	public <T> void add(String queueName, T data) {
		if (data != null) {
			IQueue<T> queue = instance.getQueue(queueName);
			queue.offer(data);
		}
	}

	public <T> void add(String queueName, Collection<T> data) {
		if (data != null) {
			IQueue<T> queue = instance.getQueue(queueName);
			data.forEach(t -> queue.offer(t));
		}
	}

	public <T> void remove(String queueName, Collection<T> data) {
		if (data != null) {
			IQueue<T> queue = instance.getQueue(queueName);
			queue.removeAll(data);
		}
	}

	public <T> Set<T> deduplicate(String queueName, Collection<T> data) {
		if (data != null) {
			IQueue<T> queue = instance.getQueue(queueName);
			return data.stream().filter(el -> !queue.contains(el)).collect(Collectors.toSet());
		}
		return null;
	}
}
