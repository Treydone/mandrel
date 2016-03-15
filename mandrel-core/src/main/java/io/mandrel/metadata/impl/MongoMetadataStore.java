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
package io.mandrel.metadata.impl;

import io.mandrel.blob.BlobMetadata;
import io.mandrel.common.bson.JsonBsonCodec;
import io.mandrel.common.bson.UriCodec;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.metadata.MetadataStore;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;

public class MongoMetadataStore extends MetadataStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class MongoMetadataStoreDefinition implements MetadataStoreDefinition {

		private static final long serialVersionUID = -9205125497698919267L;

		@JsonProperty("uri")
		private String uri = "mongodb://localhost";
		@JsonProperty("database")
		private String database = "mandrel";
		@JsonProperty("collection")
		private String collection = "metadata_{0}";
		@JsonProperty("batch_size")
		private int batchSize = 1000;

		@Override
		public String name() {
			return "mongo";
		}

		@Override
		public MongoMetadataStore build(TaskContext context) {
			CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new UriCodec()));
			MongoClientOptions.Builder options = MongoClientOptions.builder().codecRegistry(codecRegistry);
			MongoClientURI uri = new MongoClientURI(this.uri, options);
			return new MongoMetadataStore(context, new MongoClient(uri), database, MessageFormat.format(collection, context.getSpiderId()), batchSize);
		}
	}

	private final MongoClient mongoClient;

	@VisibleForTesting
	@Getter
	private final MongoCollection<org.bson.Document> collection;
	private final ObjectMapper mapper = new ObjectMapper();
	private final int batchSize;

	public MongoMetadataStore(TaskContext context, MongoClient mongoClient, String databaseName, String collectionName, int batchSize) {
		super(context);
		this.mongoClient = mongoClient;
		this.batchSize = batchSize;
		collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
	}

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void addMetadata(Uri uri, BlobMetadata metadata) {
		org.bson.Document document = JsonBsonCodec.toBson(mapper, metadata);
		document.append("_id", uri.toString());
		collection.replaceOne(Filters.eq("_id", document.getString("_id")), document, new UpdateOptions().upsert(true));
	}

	@Override
	public Set<Uri> deduplicate(Collection<Uri> uris) {
		Set<Uri> temp = Sets.newHashSet(uris);

		Set<Uri> founds = Sets.newHashSet(collection.find(Filters.in("_id", uris)).projection(Projections.include("_id"))
				.map(doc -> Uri.create(doc.getString("_id"))).iterator());
		temp.removeAll(founds);

		return temp;
	}

	@Override
	public void delete(Uri uri) {
		collection.deleteOne(Filters.eq("_id", uri.toString()));
	}

	@Override
	public BlobMetadata getMetadata(Uri uri) {
		Document doc = collection.find(Filters.eq("_id", uri.toString())).first();
		return JsonBsonCodec.fromBson(mapper, doc, BlobMetadata.class);
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
				loop = callback.on(docs.stream().map(doc -> JsonBsonCodec.fromBson(mapper, doc, BlobMetadata.class)).collect(Collectors.toList()));
			}
		} finally {
			cursor.close();
		}
	}

	@Override
	public void deleteAll() {
		collection.drop();
	}

	@Override
	public void init() {

	}

	@Override
	public void close() throws IOException {
		mongoClient.close();
	}

}
