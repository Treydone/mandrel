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

import io.mandrel.common.bson.JsonBsonCodec;
import io.mandrel.metrics.GlobalMetrics;
import io.mandrel.metrics.MetricsRepository;
import io.mandrel.metrics.NodeMetrics;
import io.mandrel.metrics.SpiderMetrics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ConditionalOnProperty(value = "engine.mongodb.enabled", matchIfMissing = true)
public class MongoMetricsRepository implements MetricsRepository {

	private final MongoClient mongoClient;
	private final MongoProperties properties;
	private final ObjectMapper mapper;
	private final Splitter splitter = Splitter.on('.').limit(2).trimResults();

	private MongoCollection<Document> counters;

	@PostConstruct
	public void init() {
		counters = mongoClient.getDatabase(properties.getMongoClientDatabase()).getCollection("counters");
	}

	public void sync(Map<String, Long> accumulators) {
		// {global.XXX=0, global.YYY=0, ...} to {global{XXX=O, YYY=0}, ...}
		Stream<Pair<String, Pair<String, Long>>> map = accumulators.entrySet().stream().map(e -> {
			Iterable<String> results = splitter.split(e.getKey());
			List<String> elts = Lists.newArrayList(results);
			return Pair.of(elts.get(0), Pair.of(elts.get(1), e.getValue()));
		});
		Map<String, List<Pair<String, Long>>> byKey = map.collect(Collectors.groupingBy(e -> e.getLeft(),
				Collectors.mapping(e -> e.getRight(), Collectors.toList())));

		List<UpdateOneModel<Document>> requests = byKey.entrySet().stream().map(e -> {
			Document updates = new Document();
			e.getValue().stream().forEach(i -> {
				Iterable<String> results = splitter.split(i.getKey());
				List<String> elts = Lists.newArrayList(results);
				if (elts.size() > 1) {
					updates.put(elts.get(0) + "." + JsonBsonCodec.toBson(elts.get(1)), i.getValue());
				} else {
					updates.put(i.getKey(), i.getValue());
				}
			});

			Pair<String, Document> bsonUpdates = Pair.of(e.getKey(), new Document("$inc", updates));
			return bsonUpdates;
		}).map(pair -> new UpdateOneModel<Document>(Filters.eq("_id", pair.getLeft()), pair.getRight(), new UpdateOptions().upsert(true)))
				.collect(Collectors.toList());

		counters.bulkWrite(requests);
	}

	public NodeMetrics node(String nodeId) {
		Document document = counters.find(Filters.eq("_id", "node_" + nodeId)).first();
		return document != null ? JsonBsonCodec.fromBson(mapper, document, NodeMetrics.class) : new NodeMetrics();
	}

	public GlobalMetrics global() {
		Document document = counters.find(Filters.eq("_id", "global")).first();
		return document != null ? JsonBsonCodec.fromBson(mapper, document, GlobalMetrics.class) : new GlobalMetrics();
	}

	public SpiderMetrics spider(long spiderId) {
		Document document = counters.find(Filters.eq("_id", "spider_" + spiderId)).first();
		return document != null ? JsonBsonCodec.fromBson(mapper, document, SpiderMetrics.class) : new SpiderMetrics();
	}

	@Override
	public void delete(long spiderId) {
		counters.deleteOne(Filters.eq("_id", spiderId));
	}
}
