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
package io.mandrel.metrics;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ConditionalOnProperty(value = "engine.mongodb.enabled", matchIfMissing = true)
public class MetricsRepository {

	private final MongoClient mongoClient;
	private final ObjectMapper mapper;

	private MongoCollection<Document> counters;

	@PostConstruct
	public void init() {
		counters = mongoClient.getDatabase("mandrel").getCollection("counters");
	}

	public void sync(HostAccumulator host) {

		// Update host and global metrics
		Document update = new Document();
		Document inc = new Document().append("nbPagesTotal", host.getNbPagesTotal().getAndSet(0)).append("totalSizeTotal",
				host.getTotalSizeTotal().getAndSet(0));
		update.append("$inc", inc);

		//
		Document statusesDoc = new Document();
		for (Entry<Integer, AtomicLong> entry : host.getStatuses().entrySet()) {
			statusesDoc.append("statuses." + entry.getKey(), entry.getValue().getAndSet(0));
		}
		update.append("$inc", statusesDoc);

		//
		Document hostsDoc = new Document();
		for (Entry<String, AtomicLong> entry : host.getHosts().entrySet()) {
			hostsDoc.append("hosts." + entry.getKey(), entry.getValue().getAndSet(0));
		}
		update.append("$inc", hostsDoc);

		//
		Document contentTypesDoc = new Document();
		for (Entry<String, AtomicLong> entry : host.getContentTypes().entrySet()) {
			contentTypesDoc.append("contentTypes." + entry.getKey(), entry.getValue().getAndSet(0));
		}
		update.append("$inc", contentTypesDoc);

		counters.updateOne(Filters.eq("_id", "global"), update, new UpdateOptions().upsert(true));
		counters.updateOne(Filters.eq("_id", host.getHostname()), update, new UpdateOptions().upsert(true));

		// Update spider metrics
		// TODO

	}

	public void sync(Map<Long, SpiderAccumulator> spiders) {

		// Update spider metrics
		// TODO

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
}
