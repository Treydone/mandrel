package io.mandrel.metrics.impl;

import java.util.Map;

import org.junit.Test;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class MongoMetricsRepositoryTest {

	@Test
	public void test() {

		MongoMetricsRepository mongoMetricsRepository = new MongoMetricsRepository(null, new MongoProperties(), new ObjectMapper());

		Map<String, Long> accumulators = Maps.newHashMap();
		accumulators.put("global.hosts.www.leboncoin.com", 0l);
		accumulators.put("global.nbPagesTotal", 0l);
		mongoMetricsRepository.sync(accumulators);
	}
}
