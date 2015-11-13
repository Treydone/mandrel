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

import io.mandrel.metrics.GlobalMetrics;
import io.mandrel.metrics.MetricsRepository;
import io.mandrel.metrics.NodeMetrics;
import io.mandrel.metrics.SpiderMetrics;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Iterables;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOptions;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ConditionalOnProperty(value = "engine.mongodb.enabled", matchIfMissing = true)
public class MongoMetricsRepository implements MetricsRepository {

	private final MongoClient mongoClient;
	private final ObjectMapper mapper;
	private final Splitter splitter = Splitter.on('.').limit(1).trimResults();

	private MongoCollection<Document> counters;

	@PostConstruct
	public void init() {
		counters = mongoClient.getDatabase("mandrel").getCollection("counters");
	}

	public void sync(Map<String, Long> accumulators) {
		Map<String, List<Pair<String, Long>>> byKey = accumulators.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue()))
				.collect(Collectors.groupingBy(e -> e.getLeft()));

		List<ReplaceOneModel<Document>> requests = byKey.entrySet().stream().map(e -> {
			Document updates = new Document();
			Iterable<String> results = splitter.split(e.getKey());
			String[] elts = Iterables.toArray(results);
			e.getValue().stream().forEach(i -> updates.append(elts[1], i.getValue()));
			return Pair.of(elts[0], new Document("$inc", updates));
		}).map(pair -> new ReplaceOneModel<Document>(Filters.eq("_id", pair.getLeft()), pair.getRight(), new UpdateOptions().upsert(true)))
				.collect(Collectors.toList());

		counters.bulkWrite(requests);
	}

	@SneakyThrows(IOException.class)
	public NodeMetrics node(String nodeId) {
		Document document = counters.find(Filters.eq("_id", nodeId)).first();
		return document != null ? mapper.readValue(document.toJson(), NodeMetrics.class) : new NodeMetrics();
	}

	@SneakyThrows(IOException.class)
	public GlobalMetrics global() {
		Document document = counters.find(Filters.eq("_id", "global")).first();
		return document != null ? mapper.readValue(document.toJson(), GlobalMetrics.class) : new GlobalMetrics();
	}

	@SneakyThrows(IOException.class)
	public SpiderMetrics spider(long spiderId) {
		Document document = counters.find(Filters.eq("_id", spiderId)).first();
		return document != null ? mapper.readValue(document.toJson(), SpiderMetrics.class) : new SpiderMetrics();
	}

	@Override
	public void delete(long spiderId) {
		counters.deleteOne(Filters.eq("_id", spiderId));
	}
}
