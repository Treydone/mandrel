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
import io.mandrel.metrics.MetricsService;
import io.mandrel.metrics.Timeserie;

import java.time.LocalDateTime;
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

public class MongoServiceTest {

	private MetricsService metricsService;
	private MongoMetricsRepository mongoMetricsRepository;

	@Before
	public void beforeEachTest() {

		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs(new LocalDateTimeCodec(), new HostAndPortCodec(), new LocalDateTimeCodec()));

		MongoProperties properties = new MongoProperties();

		MongoClient mongoClient = new MongoClient(new MongoClientURI(properties.getUri(), MongoClientOptions.builder().codecRegistry(codecRegistry)));

		mongoMetricsRepository = new MongoMetricsRepository(mongoClient, properties, new ObjectMapper());

		mongoMetricsRepository.init();

		metricsService = new MetricsService(mongoMetricsRepository);

	}

	@Test
	public void test() {

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime keytime = now.withMinute(0).withSecond(0).withNano(0);

		mongoMetricsRepository.prepareMinutes(keytime);

		Map<String, Long> accumulators = Maps.newHashMap();
		accumulators.put(MetricKeys.globalTotalSize(), 5l);
		metricsService.sync(accumulators);

		Timeserie serie = metricsService.serie(MetricKeys.globalTotalSize());
		System.err.println(serie);

	}
}
