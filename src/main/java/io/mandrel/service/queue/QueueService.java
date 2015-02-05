package io.mandrel.service.queue;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
@Slf4j
public class QueueService {

	private final HazelcastInstance instance;

	private final ExecutorService executor;

	@Inject
	public QueueService(HazelcastInstance instance, ExecutorService executor) {
		this.instance = instance;
		this.executor = executor;
	}

	public <T> void add(String queueName, T data) {
		instance.getQueue("urls").add(data);
	}

	public <T> void registrer(String queueName, Callback<T> callback) {
		executor.execute(() -> {
			while (true) {
				try {
					T message = (T) instance.getQueue(queueName).take();

					callback.onMessage(message);

				} catch (Exception e) {
					log.warn("", e);
					try {
						Thread.sleep(5000);
					} catch (Exception e1) {
						log.warn("", e1);
					}
				}
			}
		});
	}

	@FunctionalInterface
	public static interface Callback<T> {

		void onMessage(T message);
	}
}
