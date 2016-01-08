package io.mandrel.frontier.store.impl;

import io.mandrel.common.net.Uri;
import io.mandrel.frontier.store.FetchRequest;

import java.util.Map;
import java.util.Queue;

import kafka.consumer.ConsumerIterator;
import kafka.message.MessageAndMetadata;

import com.google.common.collect.Queues;

public class Dequeuer implements Runnable {

	private Queue<FetchRequest> queue = Queues.newArrayDeque();
	private Map<String, ConsumerIterator<String, Uri>> topics;

	public void fetch(FetchRequest request) {
		queue.add(request);
	}

	public void unsubscribe(String name) {
		topics.remove(name);
	}

	public void subscribe(String name, ConsumerIterator<String, Uri> topic) {
		topics.putIfAbsent(name, topic);
	}

	@Override
	public void run() {
		while (true) {
			try {
				FetchRequest request = queue.poll();

				if (request != null) {
					ConsumerIterator<String, Uri> stream = topics.get(request.getTopic());

					if (stream.hasNext()) {
						MessageAndMetadata<String, Uri> message = stream.peek();

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
