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

		String topicName = "topic_jobpouet";

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
