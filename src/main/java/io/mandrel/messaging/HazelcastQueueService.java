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
package io.mandrel.messaging;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HazelcastQueueService implements QueueService {

	private final HazelcastInstance instance;

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
				if (message != null) {
					loop = !callback.onMessage(message);
					nbSuccessiveWait = 0;
				} else {
					log.debug("No more message, waiting...");

					try {
						Thread.sleep(5000);
					} catch (Exception e1) {
						log.warn("Wut?", e1);
					}

					nbSuccessiveWait++;
					if (nbSuccessiveWait >= maxAllowedSuccessiveWait) {
						log.warn("Too many succesive wait, breaking the loop");
						break;
					}
				}
				nbSuccessiveExceptions = 0;
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
}
