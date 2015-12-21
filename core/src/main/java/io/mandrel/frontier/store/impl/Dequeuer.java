package io.mandrel.frontier.store.impl;

import io.mandrel.frontier.store.FetchRequest;

import java.net.URI;
import java.util.Map;
import java.util.Queue;

import kafka.consumer.ConsumerIterator;
import kafka.message.MessageAndMetadata;

import com.google.common.collect.Queues;

public class Dequeuer implements Runnable {

	private Queue<FetchRequest> queue = Queues.newArrayDeque();
	private Map<String, ConsumerIterator<String, URI>> topics;

	public void fetch(FetchRequest request) {
		queue.add(request);
	}

	public void unsubscribe(String name) {
		topics.remove(name);
	}

	public void subscribe(String name, ConsumerIterator<String, URI> topic) {
		topics.putIfAbsent(name, topic);
	}

	@Override
	public void run() {
		while (true) {
			try {
				FetchRequest request = queue.poll();

				if (request != null) {
					ConsumerIterator<String, URI> stream = topics.get(request.getTopic());

					if (stream.hasNext()) {
						MessageAndMetadata<String, URI> message = stream.peek();

						if (request.getCallback() != null) {
							request.getCallback().on(message.message());
						}
					}
				}
			} catch (Exception e) {
				// TODO
			}
		}
	}
}
