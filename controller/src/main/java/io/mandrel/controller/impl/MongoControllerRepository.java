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
import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Statuses;
import io.mandrel.config.BindConfiguration;
import io.mandrel.controller.ControllerRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ConditionalOnProperty(value = "engine.mongodb.enabled", matchIfMissing = true)
public class MongoControllerRepository implements ControllerRepository {

	private final IdGenerator idGenerator = new MongoIdGenerator();
	private final MongoClient client;
	private ObjectMapper mapper;

	private MongoCollection<Document> collection;

	@PostConstruct
	public void init() {
		mapper = new ObjectMapper();
		BindConfiguration.configure(mapper);
		mapper.getFactory().setCharacterEscapes(new BsonEscaper());

		collection = client.getDatabase("mandrel").getCollection("spiders");
	}

	@SneakyThrows(IOException.class)
	public Spider add(Spider spider) {
		long id = idGenerator.generateId("spiders");
		spider.setId(id);
		collection.insertOne(Document.parse(mapper.writeValueAsString(spider)));
		return spider;
	}

	@SneakyThrows(IOException.class)
	public Spider update(Spider spider) {
		collection.replaceOne(new Document("_id", spider.getId()), Document.parse(mapper.writeValueAsString(spider)));
		return spider;
	}

	public void updateStatus(long spiderId, String status) {
		collection.updateOne(new Document("_id", spiderId),
				new Document("$set", new Document("status", status).append(status, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())));
	}

	public void delete(long id) {
		collection.deleteOne(Filters.eq("_id", id));
	}

	@SneakyThrows(IOException.class)
	public Optional<Spider> get(long id) {
		Document doc = collection.find(Filters.eq("_id", id)).first();
		return doc == null ? Optional.empty() : Optional.of(mapper.readValue(doc.toJson(), Spider.class));
	}

	public Stream<Spider> list() {
		return StreamSupport.stream(collection.find().map(doc -> {
			try {
				return mapper.readValue(doc.toJson(), Spider.class);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}).spliterator(), false);
	}

	public Stream<Spider> listActive() {
		return StreamSupport.stream(collection.find(Filters.or(Filters.eq("status", Statuses.STARTED), Filters.eq("status", Statuses.PAUSED))).map(doc -> {
			try {
				return mapper.readValue(doc.toJson(), Spider.class);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}).spliterator(), false);
	}

	public static class BsonEscaper extends CharacterEscapes {

		private static final long serialVersionUID = 496536831641799617L;
		private final int[] _asciiEscapes;

		public BsonEscaper() {
			_asciiEscapes = standardAsciiEscapesForJSON();
			_asciiEscapes['.'] = CharacterEscapes.ESCAPE_CUSTOM;
		}

		@Override
		public int[] getEscapeCodesForAscii() {
			return _asciiEscapes;
		}

		@Override
		public SerializableString getEscapeSequence(int i) {
			if (i == '.') {
				return new SerializedString("->");
			} else {
				return null;
			}
		}
	}
}
