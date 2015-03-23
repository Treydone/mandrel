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
		while (true) {
			try {
				T message = (T) instance.getQueue(queueName).take();
				callback.onMessage(message);
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
		void onMessage(T message);
	}
}
