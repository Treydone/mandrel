package io.mandrel.messaging;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

@Component
@Slf4j
public class QueueService {

	private final HazelcastInstance instance;

	@Inject
	public QueueService(HazelcastInstance instance) {
		this.instance = instance;
	}

	public <T> void markAsPending(String queueName, String identifier, T data) {
		if (identifier != null) {

			if (instance.getConfig().getQueueConfigs().containsKey(queueName)) {
				// Create map of pendings with TTL of 2min
				MapConfig mapConfig = new MapConfig();
				mapConfig.setName(queueName).setBackupCount(1).setTimeToLiveSeconds(120).setStatisticsEnabled(true);
				instance.getConfig().addMapConfig(mapConfig);
			}

			IMap<String, T> pendings = instance.getMap(queueName);
			pendings.put(identifier, data);
		}
	}

	public <T> void removePending(String queueName, String identifier) {
		if (identifier != null) {
			IMap<String, T> pendings = instance.getMap(queueName);
			pendings.remove(identifier);
		}
	}

	public <T> Set<T> filterPendings(String queueName, Collection<T> identifiers) {
		if (identifiers != null) {
			IMap<String, T> pendings = instance.getMap(queueName);
			return identifiers.stream().filter(el -> !pendings.containsKey(el)).collect(Collectors.toSet());
		}
		return null;
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

	/**
	 * Blocking call (while true) on a queue.
	 * 
	 * @param queueName
	 * @param callback
	 */
	public <T> void registrer(String queueName, Callback<T> callback) {
		boolean loop = true;
		int nbSuccessiveExceptions = 0;
		// TODO should be parameterized
		int maxAllowedSuccessiveExceptions = 10;

		int nbSuccessiveWait = 0;
		// TODO should be parameterized
		int maxAllowedSuccessiveWait = 10;

		while (loop) {
			try {
				// Block until message arrive
				T message = instance.<T> getQueue(queueName).poll(5, TimeUnit.SECONDS);
				loop = !callback.onMessage(message);
				nbSuccessiveExceptions = 0;
				nbSuccessiveWait = 0;
			} catch (InterruptedException e) {
				log.trace("No more message, waiting...");

				nbSuccessiveWait++;
				if (nbSuccessiveWait >= maxAllowedSuccessiveWait) {
					log.warn("Too many succesive wait, breaking the loop");
					break;
				}

				try {
					Thread.sleep(5000);
				} catch (Exception e1) {
					log.warn("Wut?", e1);
				}
			} catch (Exception e) {
				log.warn("Wut?", e);

				nbSuccessiveExceptions++;
				if (nbSuccessiveExceptions >= maxAllowedSuccessiveExceptions) {
					log.warn("Too many succesive exceptions, breaking the loop");
					break;
				}
			}
		}
		log.warn("Loop has been stopped");
	}

	@FunctionalInterface
	public static interface Callback<T> {

		/**
		 * Return true if this has to stop
		 * 
		 * @param message
		 * @return
		 */
		boolean onMessage(T message);
	}
}
