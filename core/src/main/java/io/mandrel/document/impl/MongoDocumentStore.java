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
package io.mandrel.document.impl;

import io.mandrel.common.service.TaskContext;
import io.mandrel.data.content.MetadataExtractor;
import io.mandrel.document.Document;
import io.mandrel.document.DocumentStore;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true, fluent = true)
public class MongoDocumentStore extends DocumentStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class MongoDocumentStoreDefinition extends DocumentStoreDefinition<MongoDocumentStore> {

		private static final long serialVersionUID = -9205125497698919267L;

		@JsonProperty("uri")
		private String uri = "mongodb://localhost";
		@JsonProperty("database")
		private String database = "test";
		@JsonProperty("collection")
		private String collection = "document_{0}";
		@JsonProperty("batch_size")
		private int batchSize = 1000;

		@Override
		public String name() {
			return "mongo";
		}

		@Override
		public MongoDocumentStore build(TaskContext context) {
			MongoClientOptions.Builder options = MongoClientOptions.builder();
			// TODO options.description("");
			MongoClientURI uri = new MongoClientURI(this.uri, options);
			return new MongoDocumentStore(context, metadataExtractor, new MongoClient(uri), database, MessageFormat.format(collection, context.getSpiderId()),
					batchSize);
		}
	}

	private final MongoClient mongoClient;
	private final MongoCollection<org.bson.Document> collection;
	private final int batchSize;

	private final static Function<? super Document, ? extends org.bson.Document> toBson = entry -> {
		org.bson.Document document = new org.bson.Document();
		document.putAll(entry);
		document.put("_id", entry.getId());
		return document;
	};

	@SuppressWarnings("unchecked")
	private final static Function<? super org.bson.Document, ? extends Document> fromBson = entry -> {
		Document document = new Document();
		for (Entry<String, Object> item : entry.entrySet()) {
			document.put(item.getKey(), (List<? extends Object>) item.getValue());
		}
		document.setId(entry.getString("_id"));
		return document;
	};

	public MongoDocumentStore(TaskContext context, MetadataExtractor metadataExtractor, MongoClient mongoClient, String databaseName, String collectionName,
			int batchSize) {
		super(context, metadataExtractor);
		this.mongoClient = mongoClient;
		this.collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
		this.batchSize = batchSize;
	}

	@Override
	public void save(Document data) {
		if (data != null) {
			collection.insertOne(toBson.apply(data));
		}
	}

	@Override
	public void save(List<Document> data) {
		if (data != null) {
			collection.insertMany(data.stream().map(toBson).collect(Collectors.toList()));
		}
	}

	@Override
	public boolean check() {
		// TODO
		return true;
	}

	@Override
	public void deleteAll() {
		collection.drop();
	}

	@Override
	public void byPages(int pageSize, Callback callback) {
		MongoCursor<org.bson.Document> cursor = collection.find().iterator();
		boolean loop = true;
		try {
			while (loop) {
				List<org.bson.Document> docs = new ArrayList<>(batchSize);
				int i = 0;
				while (cursor.hasNext() && i < batchSize) {
					docs.add(cursor.next());
					i++;
				}
				loop = callback.on(docs.stream().map(fromBson).collect(Collectors.toList()));
			}
		} finally {
			cursor.close();
		}
	}

	@Override
	public long total() {
		return collection.count();
	}

	@Override
	public Collection<Document> byPages(int pageSize, int pageNumber) {
		MongoCursor<org.bson.Document> cursor = collection.find().skip(pageSize * pageNumber).limit(pageSize).iterator();
		List<org.bson.Document> docs = new ArrayList<>(10);
		while (cursor.hasNext()) {
			docs.add(cursor.next());
		}
		return docs.stream().map(fromBson).collect(Collectors.toList());
	}

	@Override
	public void init() {
	}

	@Override
	public void close() throws IOException {
		mongoClient.close();
	}
}
