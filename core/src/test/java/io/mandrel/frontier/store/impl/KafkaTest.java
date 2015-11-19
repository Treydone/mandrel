package io.mandrel.frontier.store.impl;

import java.util.Properties;

import kafka.admin.AdminUtils;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;
import kafka.utils.ZKStringSerializer$;

@Slf4j
public class KafkaTest {

	@Test
	public void test() {

		String topicName = "topic_spiderpouet";

		ZkClient zkClient = new ZkClient("localhost:2181", 10000, 10000, ZKStringSerializer$.MODULE$);

//		AdminUtils.deleteTopic(zkClient, topicName);

		if (!AdminUtils.topicExists(zkClient, topicName)) {
			log.warn("Kafka topic '{}' doesn't exists, creating it", topicName);
			AdminUtils.createTopic(zkClient, topicName, 1, 1, new Properties());
		}

		System.err.println(AdminUtils.fetchTopicConfig(zkClient, topicName));
		System.err.println(AdminUtils.fetchTopicMetadataFromZk(topicName, zkClient));
	}
}
