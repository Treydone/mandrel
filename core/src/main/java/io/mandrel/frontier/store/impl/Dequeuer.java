/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
