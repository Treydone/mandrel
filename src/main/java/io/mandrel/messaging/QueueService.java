package io.mandrel.messaging;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
@Slf4j
public class QueueService {

	private final HazelcastInstance instance;

	@Inject
	public QueueService(HazelcastInstance instance) {
		this.instance = instance;
	}

	public <T> void add(String queueName, T data) {
		instance.getQueue(queueName).add(data);
	}

	/**
	 * Blocking call (while true) on a queue.
	 * 
	 * @param queueName
	 * @param callback
	 */
	public <T> void registrer(String queueName, Callback<T> callback) {
		boolean loop = true;
		while (loop) {
			try {
				T message = instance.<T> getQueue(queueName).take();
				loop = !callback.onMessage(message);
			} catch (Exception e) {
				log.warn("Wut?", e);
				try {
					Thread.sleep(5000);
				} catch (Exception e1) {
					log.warn("Wut?", e1);
				}
			}
		}
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
