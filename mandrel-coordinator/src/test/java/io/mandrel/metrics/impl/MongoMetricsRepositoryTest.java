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
package io.mandrel.metrics.impl;

import io.mandrel.common.bson.HostAndPortCodec;
import io.mandrel.common.bson.LocalDateTimeCodec;
import io.mandrel.metrics.MetricKeys;
import io.mandrel.metrics.Timeserie;

import java.util.Map;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;

public class MongoMetricsRepositoryTest {

	private MongoMetricsRepository mongoMetricsRepository;

	@Before
	public void beforeEachTest() {

		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs(new LocalDateTimeCodec(), new HostAndPortCodec(), new LocalDateTimeCodec()));

		MongoProperties properties = new MongoProperties();

		MongoClient mongoClient = new MongoClient(new MongoClientURI(properties.getUri(), MongoClientOptions.builder().codecRegistry(codecRegistry)));

		mongoMetricsRepository = new MongoMetricsRepository(mongoClient, properties, new ObjectMapper());

		mongoMetricsRepository.init();

	}

	@Test
	public void test() {

		Map<String, Long> accumulators = Maps.newHashMap();
		accumulators.put("global.hosts.www.leboncoin.com", 1l);
		accumulators.put("global.nbPagesTotal", 1l);
		accumulators.put("job_1.hosts.www.leboncoin.com", 1l);
		accumulators.put("job_1.statuses.200", 5l);
		accumulators.put("node_1.hosts.www.leboncoin.com", 1l);
		accumulators.put("node_1.statuses.200", 5l);
		mongoMetricsRepository.sync(accumulators);
	}

	@Test
	public void test2() {

		Map<String, Long> accumulators = Maps.newHashMap();
		accumulators.put("global.totalSizeTotal", 5l);
		accumulators.put("node_1.hosts.www.leboncoin.com", 1l);
		accumulators.put("node_1.totalSizeTotal", 5l);
		mongoMetricsRepository.sync(accumulators);

		Timeserie serie = mongoMetricsRepository.serie("totalSizeTotal");
		System.err.println(serie);

	}

	@Test
	public void prepare() {

		mongoMetricsRepository.prepareNextMinutes();

		Timeserie serie = mongoMetricsRepository.serie(MetricKeys.globalTotalSize());
		System.err.println(serie);

	}
}
