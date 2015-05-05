package io.mandrel.messaging;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

@Component
@Slf4j
public class QueueService {

	private final HazelcastInstance instance;

	@Inject
	public QueueService(HazelcastInstance instance) {
		this.instance = instance;
	}

	public <T> void add(String queueName, Collection<T> data) {
		if (data != null) {
			IQueue<Object> queue = instance.getQueue(queueName);
			data.forEach(t -> queue.offer(t));
		}
	}

	public <T> Set<T> deduplicate(String queueName, Collection<T> data) {
		if (data != null) {
			IQueue<T> queue = instance.getQueue(queueName);
			return data.stream().filter(queue::contains).collect(Collectors.toSet());
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
		int nbException = 0;
		// TODO should be parameterized
		int maxAllowedSuccessiveException = 10;

		while (loop) {
			try {
				// Block until message arrive
				T message = instance.<T> getQueue(queueName).take();
				loop = !callback.onMessage(message);
			} catch (Exception e) {
				log.warn("Wut?", e);

				nbException++;
				if (nbException >= maxAllowedSuccessiveException) {
					log.warn("Too many succesive exceptions, breaking the loop");
					break;
				}

				try {
					Thread.sleep(5000);
				} catch (Exception e1) {
					log.warn("Wut?", e1);
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
