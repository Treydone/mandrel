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

import io.mandrel.common.bson.JsonBsonCodec;
import io.mandrel.common.bson.UriCodec;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.metadata.FetchMetadata;
import io.mandrel.metadata.MetadataStore;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;

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
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

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

		@Override
		public String name() {
			return "mongo";
		}

		@Override
		public MongoMetadataStore build(TaskContext context) {
			CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new UriCodec()));
			MongoClientOptions.Builder options = MongoClientOptions.builder().codecRegistry(codecRegistry);
			MongoClientURI uri = new MongoClientURI(this.uri, options);
			return new MongoMetadataStore(context, new MongoClient(uri), database, MessageFormat.format(collection, context.getSpiderId()));
		}
	}

	private final MongoClient mongoClient;

	@VisibleForTesting
	@Getter
	private final MongoCollection<org.bson.Document> collection;
	private final ObjectMapper mapper = new ObjectMapper();

	public MongoMetadataStore(TaskContext context, MongoClient mongoClient, String databaseName, String collectionName) {
		super(context);
		this.mongoClient = mongoClient;
		collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
	}

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void addMetadata(Uri uri, FetchMetadata metadata) {
		org.bson.Document document = JsonBsonCodec.toBson(mapper, metadata);
		document.append("_id", uri.toString());
		collection.insertOne(document);
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
	public FetchMetadata getMetadata(Uri uri) {
		Document doc = collection.find(Filters.eq("_id", uri.toString())).first();
		return JsonBsonCodec.fromBson(mapper, doc, FetchMetadata.class);
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
