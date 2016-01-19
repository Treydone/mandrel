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
import io.mandrel.common.kafka.JsonSerializer;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.frontier.store.FetchRequest;
import io.mandrel.frontier.store.FrontierStore;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import kafka.admin.AdminUtils;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
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
import org.apache.kafka.common.serialization.StringSerializer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

@Slf4j
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
			// properties.put(ConsumerConfig.SESSION_TIMEOUT_MS, "30000");
			properties.put(ConsumerConfig.GROUP_ID_CONFIG, "mandrel");
			// properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
			// StringDeserializer.class.getName());
			// properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
			// JsonDeserializer.class.getName());
			// properties.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY,
			// "range");

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

	private final int partitions;
	private final int replicationFactor;
	private final ZkClient zkClient;
	private final ZkUtils zkUtils;

	private final Producer<String, Uri> producer;
	private final ConsumerConnector consumer;
	private final ExecutorService executor;
	private final List<Dequeuer> workers;
	private final AtomicInteger nextWorker;
	private final int nbWorker;

	public KafkaFrontierStore(TaskContext context, Properties properties, int partitions, int replicationFactor, int sessionTimeout, int connectionTimeout) {
		super(context);
		this.partitions = partitions;
		this.replicationFactor = replicationFactor;
		this.zkClient = new ZkClient(properties.getProperty("zookeeper.connect"), sessionTimeout, connectionTimeout, ZKStringSerializer$.MODULE$);
		this.zkUtils = ZkUtils.apply(zkClient, false);
		this.producer = new KafkaProducer<>(properties);

		consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new kafka.consumer.ConsumerConfig(properties));

		nbWorker = Runtime.getRuntime().availableProcessors();

		executor = Executors.newFixedThreadPool(nbWorker);
		workers = Lists.newArrayListWithCapacity(nbWorker);
		IntStream.range(0, nbWorker).forEach(i -> workers.add(new Dequeuer()));

		workers.stream().forEach(worker -> executor.submit(worker));
		nextWorker = new AtomicInteger(0);
	}

	public void pool(FetchRequest request) {
		int workerId = nextWorker.getAndIncrement() % workers.size();
		Dequeuer worker = workers.get(workerId);
		worker.fetch(FetchRequest.of(getTopicName(request.getTopic()), request.getCallback()));
	}

	public void schedule(String name, Uri item) {
		producer.send(new ProducerRecord<String, Uri>(getTopicName(name), item));
	}

	public void schedule(String name, Set<Uri> items) {
		items.stream().map(item -> new ProducerRecord<String, Uri>(getTopicName(name), item)).forEach(producer::send);
	}

	private String getTopicName(String name) {
		return "topic_" + context.getSpiderId() + "_" + name;
	}

	@Override
	public void destroy(String name) {
		String topicName = getTopicName(name);
		// Check if topic exists
		if (AdminUtils.topicExists(zkUtils, topicName)) {
			log.warn("Deleting kafka topic '{}'", topicName);
			AdminUtils.deleteTopic(zkUtils, topicName);
		}

		// Delete consumer on each worker
		workers.stream().forEach(worker -> worker.unsubscribe(topicName));
	}

	@Override
	public void create(String name) {
		String topicName = getTopicName(name);

		// Check if topic already exists
		if (!AdminUtils.topicExists(zkUtils, topicName)) {
			log.warn("Kafka topic '{}' doesn't exists, creating it", topicName);
			AdminUtils.createTopic(zkUtils, topicName, partitions, replicationFactor, new Properties());
		}
		log.debug("Kafka topic '{}' found with configuration: {}", topicName, AdminUtils.fetchTopicMetadataFromZk(topicName, zkUtils).toString());

		// Prepare consumers
		Map<String, List<KafkaStream<String, Uri>>> consumerMap = consumer.createMessageStreams(Collections.singletonMap(topicName, nbWorker),
				new StringDecoder(null), new JsonDecoder<Uri>(Uri.class));

		// Add the consumer to each worker
		IntStream.range(0, nbWorker).forEach(i -> workers.get(i).subscribe(topicName, consumerMap.get(topicName).get(i).iterator()));
	}

	public void close() {
		try {
			if (consumer != null)
				consumer.shutdown();
		} catch (Exception e) {
			log.info("", e);
		}
		try {
			if (producer != null)
				producer.close();
		} catch (Exception e) {
			log.info("", e);
		}

		try {
			if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
				log.info("Timed out waiting for consumer threads to shut down, exiting uncleanly");
			}
		} catch (InterruptedException e) {
			log.info("Interrupted during shutdown, exiting uncleanly");
		}
	}

	@Override
	public boolean check() {
		// TODO
		return true;
	}
}
