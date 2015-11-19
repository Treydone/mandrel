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
package io.mandrel.controller.impl;

import io.mandrel.cluster.idgenerator.IdGenerator;
import io.mandrel.cluster.idgenerator.MongoIdGenerator;
import io.mandrel.common.bson.JsonBsonCodec;
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Statuses;
import io.mandrel.controller.SpiderRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ConditionalOnProperty(value = "engine.mongodb.enabled", matchIfMissing = true)
public class MongoSpiderRepository implements SpiderRepository {

	private final IdGenerator idGenerator = new MongoIdGenerator();
	private final MongoClient client;
	private final ObjectMapper mapper;

	private MongoCollection<Document> collection;

	@PostConstruct
	public void init() {
		collection = client.getDatabase("mandrel").getCollection("spiders");
	}

	public Spider add(Spider spider) {
		long id = idGenerator.generateId("spiders");
		spider.setId(id);
		collection.insertOne(JsonBsonCodec.toBson(mapper, spider));
		return spider;
	}

	public Spider update(Spider spider) {
		collection.replaceOne(new Document("_id", spider.getId()), JsonBsonCodec.toBson(mapper, spider));
		return spider;
	}

	public void updateStatus(long spiderId, String status) {
		collection.updateOne(new Document("_id", spiderId),
				new Document("$set", new Document("status", status).append(status, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())));
	}

	public void delete(long id) {
		collection.deleteOne(Filters.eq("_id", id));
	}

	public Optional<Spider> get(long id) {
		Document doc = collection.find(Filters.eq("_id", id)).first();
		return doc == null ? Optional.empty() : Optional.of(JsonBsonCodec.fromBson(mapper, doc, Spider.class));
	}

	public Stream<Spider> list() {
		return StreamSupport.stream(collection.find().map(doc -> JsonBsonCodec.fromBson(mapper, doc, Spider.class)).spliterator(), false);
	}

	public Stream<Spider> listActive() {
		return StreamSupport.stream(
				collection.find(Filters.or(Filters.eq("status", Statuses.STARTED), Filters.eq("status", Statuses.PAUSED)))
						.map(doc -> JsonBsonCodec.fromBson(mapper, doc, Spider.class)).spliterator(), false);
	}
}
