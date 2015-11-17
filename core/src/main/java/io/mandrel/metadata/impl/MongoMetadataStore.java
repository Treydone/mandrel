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

import io.mandrel.common.service.TaskContext;
import io.mandrel.metadata.FetchMetadata;
import io.mandrel.metadata.MetadataStore;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
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

		private String uri = "mongodb://localhost";
		private String database = "test";
		private String collection = "metadata_{0}";

		@Override
		public String name() {
			return "mongo";
		}

		@Override
		public MetadataStore build(TaskContext context) {
			MongoClientOptions.Builder options = MongoClientOptions.builder();
			// TODO options.description("");
			MongoClientURI uri = new MongoClientURI(this.uri, options);
			return new MongoMetadataStore(context, new MongoClient(uri), database, MessageFormat.format(collection, context.getSpiderId()));
		}
	}

	private final MongoClient mongoClient;
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
	@SneakyThrows(IOException.class)
	public void addMetadata(URI uri, FetchMetadata metadata) {
		String json = mapper.writeValueAsString(metadata);
		org.bson.Document document = org.bson.Document.parse(json);
		// TODO HASH?
		document.append("_id", uri);
		collection.insertOne(document);
	}

	@Override
	public Set<URI> deduplicate(Collection<URI> uris) {
		Set<URI> temp = Sets.newHashSet(uris);
		temp.removeAll(Sets.newHashSet(collection.find(Filters.in("_id", uris)).projection(Projections.include("_id"))
				.map(doc -> URI.create(doc.getString("_id"))).iterator()));
		return temp;
	}

	@Override
	@SneakyThrows(IOException.class)
	public FetchMetadata getMetadata(URI uri) {
		Document doc = collection.find(Filters.eq("_id", uri)).first();
		FetchMetadata fetchMetadata = mapper.readValue(doc.toJson(), FetchMetadata.class);
		return fetchMetadata;
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
