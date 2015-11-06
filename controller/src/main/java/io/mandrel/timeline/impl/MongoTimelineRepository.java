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
package io.mandrel.timeline.impl;

import io.mandrel.timeline.Event;
import io.mandrel.timeline.TimelineRepository;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.mongodb.CursorType;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ConditionalOnProperty(value = "engine.mongodb.enabled", matchIfMissing = true)
@Slf4j
public class MongoTimelineRepository implements TimelineRepository {

	private final MongoClient mongoClient;
	private final ObjectMapper mapper;

	private MongoCollection<Document> timeline;

	@PostConstruct
	public void init() {
		timeline = mongoClient.getDatabase("mandrel").getCollection("timeline");
	}

	@Override
	@SneakyThrows(IOException.class)
	public void add(Event event) {
		timeline.insertOne(Document.parse(mapper.writeValueAsString(event)));
	}

	@Override
	public List<Event> page(int from, int size) {
		return Lists.newArrayList(timeline.find().sort(Sorts.descending("time")).skip(from).limit(size).map(doc -> {
			try {
				return mapper.readValue(doc.toJson(), Event.class);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}));
	}

	@Override
	public void pool(Listener listener) {

		Date date = new Date();
		Bson query = new Document();

		while (true) {
			MongoCursor<Document> cursor = timeline.find(query).cursorType(CursorType.TailableAwait).iterator();

			while (true) {
				if (!cursor.hasNext()) {
					if (cursor.getServerCursor() == null) {
						break;
					}
					continue;
				}

				Document result = cursor.next();
				date = result.getDate("time");

				try {
					Event event = mapper.readValue(result.toJson(), Event.class);
					listener.on(event);
				} catch (Exception e) {
					log.warn("Error while getting the event", e);
				}
			}

			query = Filters.gt("time", date);
		}
	}
}
