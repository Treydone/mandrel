package io.mandrel.frontier.store.impl;

import java.util.Properties;

import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;

@Slf4j
public class KafkaTest {

	@Test
	public void test() {

		String topicName = "topic_spiderpouet";

		ZkClient zkClient = new ZkClient("localhost:2181", 10000, 10000, ZKStringSerializer$.MODULE$);

		ZkUtils zkUtils = ZkUtils.apply(zkClient, false);

		// AdminUtils.deleteTopic(zkClient, topicName);

		if (!AdminUtils.topicExists(zkUtils, topicName)) {
			log.warn("Kafka topic '{}' doesn't exists, creating it", topicName);
			AdminUtils.createTopic(zkUtils, topicName, 1, 1, new Properties());
		}

		System.err.println(AdminUtils.fetchTopicMetadataFromZk(topicName, zkUtils));
	}
}
