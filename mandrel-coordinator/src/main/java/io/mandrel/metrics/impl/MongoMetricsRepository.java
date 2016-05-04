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
import io.mandrel.common.mongo.MongoUtils;
import io.mandrel.metrics.GlobalMetrics;
import io.mandrel.metrics.MetricKeys;
import io.mandrel.metrics.MetricsRepository;
import io.mandrel.metrics.NodeMetrics;
import io.mandrel.metrics.JobMetrics;
import io.mandrel.metrics.Timeserie;
import io.mandrel.metrics.Timeserie.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
@ConditionalOnProperty(value = "engine.mongodb.enabled", matchIfMissing = true)
public class MongoMetricsRepository implements MetricsRepository {

	private static final String INDEX_NAME = "_time_and_type_";
	private static final List<String> TIMESERIES_ALLOWED_KEYS = Arrays.asList(MetricKeys.globalTotalSize(), MetricKeys.globalNbPages());
	private final MongoClient mongoClient;
	private final MongoProperties properties;
	private final ObjectMapper mapper;
	private final Splitter splitter = Splitter.on('.').limit(2).trimResults();

	private MongoCollection<Document> counters;
	private MongoCollection<Document> timeseries;

	private int size = 5 * 1024 * 1024;
	private int maxDocuments = 30 * 24; // 30 days

	@PostConstruct
	public void init() {
		MongoDatabase database = mongoClient.getDatabase(properties.getMongoClientDatabase());

		counters = database.getCollection("counters");
		timeseries = database.getCollection("timeseries");

		checkFilled();

		MongoUtils.checkCapped(database, "timeseries", size, maxDocuments, false);
		timeseries = database.getCollection("timeseries");

		List<Document> indexes = Lists.newArrayList(database.getCollection("timeseries").listIndexes());
		List<String> indexNames = indexes.stream().map(doc -> doc.getString("name")).collect(Collectors.toList());
		if (!indexNames.contains(INDEX_NAME)) {
			log.warn("Index on field time and type is missing, creating it. Exisiting indexes: {}", indexes);
			database.getCollection("timeseries").createIndex(new Document("timestamp_hour", 1).append("type", 1),
					new IndexOptions().name(INDEX_NAME).unique(true));
		}
	}

	public void checkFilled() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime keytime = now.withMinute(0).withSecond(0).withNano(0);
		if (TIMESERIES_ALLOWED_KEYS.stream().anyMatch(key -> {
			Document serie = timeseries.find(Filters.and(Filters.eq("type", key), Filters.eq("timestamp_hour", keytime))).limit(1).first();
			if (serie != null) {
				Map<String, Long> values = (Map<String, Long>) serie.get("values");
				if (values.size() != 60) {
					log.warn("Wrong values size for timeserie collection {}", key);
					return true;
				}
				return false;
			}
			return false;
		})) {
			log.warn("Dropping the timeseries collection");
			timeseries.drop();
		}

		List<? extends WriteModel<Document>> requests = TIMESERIES_ALLOWED_KEYS
				.stream()
				.map(key -> Pair.of(key, timeseries.find(Filters.and(Filters.eq("type", key), Filters.eq("timestamp_hour", keytime))).limit(1).first()))
				.filter(doc -> doc.getRight() == null)
				.map(pair -> pair.getLeft())
				.map(key -> {
					Document document = new Document();
					document.append("type", key).append("timestamp_hour", keytime);
					document.append("values",
							IntStream.range(0, 60).collect(Document::new, (doc, val) -> doc.put(Integer.toString(val), Long.valueOf(0)), Document::putAll));
					return document;
				})
				.map(doc -> new UpdateOneModel<Document>(Filters.and(Filters.eq("type", doc.getString("type")), Filters.eq("timestamp_hour", keytime)),
						new Document("$set", doc), new UpdateOptions().upsert(true))).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(requests)) {
			timeseries.bulkWrite(requests);
		}
	}

	@Override
	public void sync(Map<String, Long> accumulators) {

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime keytime = now.withMinute(0).withSecond(0).withNano(0);

		// {global.XXX=0, global.YYY=0, ...} to {global{XXX=O, YYY=0}, ...}
		Stream<Pair<String, Pair<String, Long>>> map = accumulators.entrySet().stream().map(e -> {
			Iterable<String> results = splitter.split(e.getKey());
			List<String> elts = Lists.newArrayList(results);
			return Pair.of(elts.get(0), Pair.of(elts.get(1), e.getValue()));
		});
		Map<String, List<Pair<String, Long>>> byKey = map.collect(Collectors.groupingBy(e -> e.getLeft(),
				Collectors.mapping(e -> e.getRight(), Collectors.toList())));

		List<? extends WriteModel<Document>> requests = byKey.entrySet().stream().map(e -> {
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

			return new UpdateOneModel<Document>(Filters.eq("_id", e.getKey()), new Document("$inc", updates), new UpdateOptions().upsert(true));
		}).collect(Collectors.toList());

		counters.bulkWrite(requests);

		requests = byKey
				.entrySet()
				.stream()
				.map(e -> {
					List<UpdateOneModel<Document>> tsUpdates = Lists.newArrayList();

					e.getValue()
							.stream()
							.forEach(
									i -> {
										Iterable<String> results = splitter.split(i.getKey());
										List<String> elts = Lists.newArrayList(results);

										if (elts.size() == 1 && e.getKey().equalsIgnoreCase(MetricKeys.global())) {
											tsUpdates.add(new UpdateOneModel<Document>(Filters.and(
													Filters.eq("type", e.getKey() + MetricKeys.METRIC_DELIM + i.getKey()),
													Filters.eq("timestamp_hour", keytime)), new Document("$inc", new Document("values."
													+ Integer.toString(now.getMinute()), i.getValue())), new UpdateOptions().upsert(true)));
										}
									});

					return tsUpdates;
				}).flatMap(list -> list.stream()).collect(Collectors.toList());

		timeseries.bulkWrite(requests);

	}

	// Every hour
	@Scheduled(cron = "0 0 * * * *")
	public void prepareNextMinutes() {
		log.debug("Preparing next metric bucket");

		// TODO Distributed lock!
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime keytime = now.withMinute(0).withSecond(0).withNano(0).plusHours(1);

		prepareMinutes(keytime);
	}

	public void prepareMinutes(LocalDateTime keytime) {
		List<? extends WriteModel<Document>> requests = TIMESERIES_ALLOWED_KEYS
				.stream()
				.map(el -> {
					Document document = new Document();
					document.append("type", el).append("timestamp_hour", keytime);
					document.append("values",
							IntStream.range(0, 60).collect(Document::new, (doc, val) -> doc.put(Integer.toString(val), Long.valueOf(0)), Document::putAll));
					return document;
				})
				.map(doc -> new UpdateOneModel<Document>(Filters.and(Filters.eq("type", doc.getString("type")), Filters.eq("timestamp_hour", keytime)),
						new Document("$set", doc), new UpdateOptions().upsert(true))).collect(Collectors.toList());

		timeseries.bulkWrite(requests);
	}

	@Override
	public NodeMetrics node(String nodeId) {
		Document document = counters.find(Filters.eq("_id", MetricKeys.node(nodeId))).first();
		return document != null ? JsonBsonCodec.fromBson(mapper, document, NodeMetrics.class) : new NodeMetrics();
	}

	@Override
	public GlobalMetrics global() {
		Document document = counters.find(Filters.eq("_id", MetricKeys.global())).first();
		return document != null ? JsonBsonCodec.fromBson(mapper, document, GlobalMetrics.class) : new GlobalMetrics();
	}

	@Override
	public JobMetrics job(long jobId) {
		Document document = counters.find(Filters.eq("_id", MetricKeys.job(jobId))).first();
		return document != null ? JsonBsonCodec.fromBson(mapper, document, JobMetrics.class) : new JobMetrics();
	}

	@Override
	public void delete(long jobId) {
		counters.deleteOne(Filters.eq("_id", jobId));
	}

	@Override
	public Timeserie serie(String name) {
		Set<Data> results = StreamSupport
				.stream(timeseries
						.find(Filters.eq("type", name))
						.sort(Sorts.ascending("timestamp_hour"))
						.limit(3)
						.map(doc -> {
							LocalDateTime hour = LocalDateTime.ofEpochSecond(((Date) doc.get("timestamp_hour")).getTime() / 1000, 0, ZoneOffset.UTC);
							Map<String, Long> values = (Map<String, Long>) doc.get("values");

							List<Data> mapped = values.entrySet().stream().map(elt -> Data.of(hour.plusMinutes(Long.valueOf(elt.getKey())), elt.getValue()))
									.collect(Collectors.toList());
							return mapped;
						}).spliterator(), true).flatMap(elts -> elts.stream()).collect(TreeSet::new, Set::add, (left, right) -> {
					left.addAll(right);
				});

		Timeserie timeserie = new Timeserie();
		timeserie.addAll(results);
		return timeserie;
	}
}
