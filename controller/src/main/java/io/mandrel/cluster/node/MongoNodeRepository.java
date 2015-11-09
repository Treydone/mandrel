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
package io.mandrel.cluster.node;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOptions;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ConditionalOnProperty(value = "engine.mongodb.enabled", matchIfMissing = true)
public class MongoNodeRepository implements NodeRepository {

	private final MongoClient client;
	private final ObjectMapper mapper;

	private MongoCollection<Document> collection;

	@PostConstruct
	public void init() {
		collection = client.getDatabase("mandrel").getCollection("nodes");
	}

	@Override
	public Collection<Node> findAll() {
		return Lists.newArrayList(collection.find().map(doc -> {
			try {
				return mapper.readValue(doc.toJson(), Node.class);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}));
	}

	@Override
	@SneakyThrows(IOException.class)
	public Optional<Node> get(URI uri) {
		Document doc = collection.find(Filters.eq("_id", uri)).first();
		return doc == null ? Optional.empty() : Optional.of(mapper.readValue(doc.toJson(), Node.class));
	}

	@Override
	public Collection<Node> findAll(Collection<URI> uris) {
		return Lists.newArrayList(collection.find(Filters.in("_id", uris)).map(doc -> {
			try {
				return mapper.readValue(doc.toJson(), Node.class);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}));
	}

	@Override
	public void update(List<Node> nodes) {
		collection.bulkWrite(nodes
				.stream()
				.map(node -> {
					try {
						return new ReplaceOneModel<Document>(Filters.eq("_id", node.getUri()), Document.parse(mapper.writeValueAsString(node)),
								new UpdateOptions().upsert(true));
					} catch (Exception e) {
						throw Throwables.propagate(e);
					}
				}).collect(Collectors.toList()));
	}
}
