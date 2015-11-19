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

import io.mandrel.common.kafka.JsonDeserializer;
import io.mandrel.common.kafka.JsonSerializer;
import io.mandrel.common.service.TaskContext;
import io.mandrel.frontier.store.FrontierStore;
import io.mandrel.frontier.store.Queue;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import kafka.admin.AdminUtils;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.utils.ZKStringSerializer$;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaFrontierStore extends FrontierStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class KafkaFrontierStoreDefinition extends FrontierStoreDefinition<KafkaFrontierStore> {
		private static final long serialVersionUID = -5715057009212205361L;

		@JsonIgnore
		private Properties properties = new Properties();

		@JsonProperty("partitions")
		private int partitions = 1;
		@JsonProperty("replication_factor")
		private int replicationFactor = 1;

		@JsonProperty("session_timeout")
		private int sessionTimeout = 10000;
		@JsonProperty("connection_timeout")
		private int connectionTimeout = 10000;

		public KafkaFrontierStoreDefinition() {

			properties.put("zookeeper.connect", "127.0.0.1:2181");
			properties.put("zookeeper.connectiontimeout.ms", "1000000");
			properties.put("zookeeper.session.timeout.ms", "400");
			properties.put("zookeeper.sync.time.ms", "200");

			properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
			properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
			properties.put(ConsumerConfig.SESSION_TIMEOUT_MS, "30000");
			properties.put(ConsumerConfig.GROUP_ID_CONFIG, "mandrel");
			properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
			properties.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY, "range");

			properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
			properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
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
			return new KafkaFrontierStore(context, properties, partitions, replicationFactor, sessionTimeout, connectionTimeout);
		}
	}

	private final Properties properties;
	private final int partitions;
	private final int replicationFactor;
	private final int sessionTimeout;
	private final int connectionTimeout;

	public KafkaFrontierStore(TaskContext context, Properties properties, int partitions, int replicationFactor, int sessionTimeout, int connectionTimeout) {
		super(context);
		this.properties = properties;
		this.partitions = partitions;
		this.replicationFactor = replicationFactor;
		this.sessionTimeout = sessionTimeout;
		this.connectionTimeout = connectionTimeout;
	}

	@Override
	public KafkaQueue<URI> create(String name) {
		String topicName = "topic_" + context.getSpiderId() + "_" + name;
		return new KafkaQueue<>(topicName, URI.class, properties, partitions, replicationFactor, sessionTimeout, connectionTimeout);
	}

	@Slf4j
	public static class KafkaQueue<T> implements Queue<T> {

		private final String name;
		private final Class<T> clazz;
		private final Producer<String, T> producer;
		// private final Consumer<String, URI> consumer;

		// TODO TO BE REMOVED
		private final ConsumerIterator<byte[], byte[]> stream;
		private final ObjectMapper mapper = new ObjectMapper();

		public KafkaQueue(String name, Class<T> clazz, Properties properties, int partitions, int replicationFactor, int sessionTimeout, int connectionTimeout) {
			super();
			this.name = name;
			this.clazz = clazz;

			properties.put("json.class", clazz);

			// Check if topic already exists
			ZkClient zkClient = new ZkClient(properties.getProperty("zookeeper.connect"), sessionTimeout, connectionTimeout, ZKStringSerializer$.MODULE$);
			if (!AdminUtils.topicExists(zkClient, name)) {
				log.warn("Kafka topic '{}' doesn't exists, creating it", name);
				AdminUtils.createTopic(zkClient, name, partitions, replicationFactor, properties);
			}
			log.debug("Kafka topic '{}' found with configuration: {}", name, AdminUtils.fetchTopicMetadataFromZk(name, zkClient).toString());

			this.producer = new KafkaProducer<>(properties);
			// consumer = new KafkaConsumer<>(properties);

			// TODO TO BE REMOVED
			ConsumerConnector connector = kafka.consumer.Consumer.createJavaConsumerConnector(new kafka.consumer.ConsumerConfig(properties));
			Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
			topicCountMap.put(name, new Integer(1));
			Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = connector.createMessageStreams(topicCountMap);
			this.stream = consumerMap.get(name).get(0).iterator();
		}

		@Override
		public T pool() {
			// ConsumerRecords<String, URI> records =
			// consumer.poll(3000).get(name);
			// for (ConsumerRecord<Integer, String> record : records) {
			// System.out.println("Received message: (" + record.key() + ", " +
			// record.value() + ") at offset " + record.offset());
			// }

			if (stream.hasNext()) {
				try {
					return mapper.readValue(stream.next().message(), clazz);
				} catch (Exception e) {
					log.info("Can not parse message:", e);
				}
			}
			return null;
		}

		@Override
		public void schedule(T item) {
			producer.send(new ProducerRecord<String, T>(name, item));
		}

		@Override
		public void schedule(Set<T> items) {
			items.stream().map(item -> new ProducerRecord<String, T>(name, item)).forEach(producer::send);
		}

		@Override
		public void close() throws IOException {
			// try {
			// consumer.close();
			// } catch (Exception e) {
			// log.info("", e);
			// }
			try {
				producer.close();
			} catch (Exception e) {
				log.info("", e);
			}
		}
	}

	@Override
	public boolean check() {
		// TODO
		return true;
	}
}
