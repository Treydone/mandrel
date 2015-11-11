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

import io.mandrel.common.kafka.JsonDecoder;
import io.mandrel.common.kafka.JsonEncoder;
import io.mandrel.common.service.TaskContext;
import io.mandrel.frontier.store.FrontierStore;
import io.mandrel.frontier.store.Queue;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringDecoder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaFrontierStore extends FrontierStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class KafkaFrontierStoreDefinition extends FrontierStoreDefinition<KafkaFrontierStore> {
		private static final long serialVersionUID = -5715057009212205361L;

		private Properties properties = new Properties();

		public KafkaFrontierStoreDefinition() {
			properties.put("group.id", "mandrel");
			properties.put("zookeeper.connect", "127.0.0.1:2181");
			properties.put("zookeeper.connectiontimeout.ms", "1000000");
			properties.put("zookeeper.session.timeout.ms", "400");
			properties.put("zookeeper.sync.time.ms", "200");
			properties.put("auto.commit.interval.ms", "1000");
			properties.put("serializer.class", JsonEncoder.class);
		}

		@JsonAnyGetter
		public Properties any() {
			return properties;
		}

		@JsonAnySetter
		public void set(String name, String value) {
			properties.put(name, value);
		}

		@Override
		public String name() {
			return "kafka";
		}

		@Override
		public KafkaFrontierStore build(TaskContext context) {

			ProducerConfig config = new ProducerConfig(properties);
			Producer<String, URI> producer = new Producer<String, URI>(config);

			ConsumerConfig consumerConfig = new ConsumerConfig(properties);
			ConsumerConnector consumer = Consumer.createJavaConsumerConnector(consumerConfig);

			return new KafkaFrontierStore(context, producer, consumer);
		}
	}

	private final Producer<String, URI> producer;
	private final ConsumerConnector consumer;
	private final ObjectMapper mapper = new ObjectMapper();

	public KafkaFrontierStore(TaskContext context, Producer<String, URI> producer, ConsumerConnector consumer) {
		super(context);
		this.producer = producer;
		this.consumer = consumer;
	}

	@Override
	public Queue<URI> create(String name) {
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(name, new Integer(1)); // TODO only 1?

		Map<String, List<KafkaStream<String, URI>>> consumerMap = consumer.createMessageStreams(topicCountMap, new StringDecoder(null), new JsonDecoder<>(
				URI.class));
		KafkaStream<String, URI> stream = consumerMap.get(name).get(0);

		return new KafkaQueue<>(name, producer, stream.iterator(), mapper);
	}

	@Data
	public static class KafkaQueue<T> implements Queue<T> {

		private final String name;
		private final Producer<String, T> producer;
		private final ConsumerIterator<String, T> consumer;
		private final ObjectMapper mapper;

		@Override
		public T pool() {
			return consumer.next().message();
		}

		@Override
		public void schedule(T item) {
			producer.send(new KeyedMessage<String, T>(name, item));
		}

		@Override
		public void schedule(Set<T> items) {
			producer.send(items.stream().map(item -> new KeyedMessage<String, T>(name, item)).collect(Collectors.toList()));
		}
	}

	@Override
	public boolean check() {
		// TODO
		return true;
	}
}
