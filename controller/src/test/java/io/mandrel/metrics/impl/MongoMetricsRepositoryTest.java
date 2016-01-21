package io.mandrel.metrics.impl;

import java.util.Map;

import org.junit.Test;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.mongodb.MongoClient;

public class MongoMetricsRepositoryTest {

	@Test
	public void test() {

		MongoMetricsRepository mongoMetricsRepository = new MongoMetricsRepository(new MongoClient(), new MongoProperties(), new ObjectMapper());
		mongoMetricsRepository.init();

		Map<String, Long> accumulators = Maps.newHashMap();
		accumulators.put("global.hosts.www.leboncoin.com", 1l);
		accumulators.put("global.nbPagesTotal", 1l);
		accumulators.put("spider_1.hosts.www.leboncoin.com", 1l);
		accumulators.put("spider_1.statuses.200", 5l);
		accumulators.put("node_1.hosts.www.leboncoin.com", 1l);
		accumulators.put("node_1.statuses.200", 5l);
		mongoMetricsRepository.sync(accumulators);
	}
}
