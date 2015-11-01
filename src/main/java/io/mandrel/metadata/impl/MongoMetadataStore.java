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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class MongoMetadataStore extends MetadataStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class MongoMetadataStoreDefinition implements MetadataStoreDefinition {

		private static final long serialVersionUID = -9205125497698919267L;

		private String uri;
		private String database;
		private String collection;

		@Override
		public String name() {
			return "mongo";
		}

		@Override
		public MetadataStore build(TaskContext context) {
			MongoClientOptions.Builder options = MongoClientOptions.builder();
			// TODO options.description("");
			MongoClientURI uri = new MongoClientURI(this.uri, options);
			return new MongoMetadataStore(context, new MongoClient(uri), database, collection);
		}
	}

	private final MongoClient mongoClient;
	private final MongoCollection<org.bson.Document> collection;

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
	public void addMetadata(URI uri, FetchMetadata webPage) {
		org.bson.Document document = new org.bson.Document();
		document.append("_id", uri);
		document.append("statusCode", webPage.statusCode());
		// TODO
		collection.insertOne(document);
	}

	@Override
	public FetchMetadata getMetadata(URI uri) {
		Document doc = collection.find(Filters.eq("_id", uri)).first();
		FetchMetadata fetchMetadata = new FetchMetadata();
		fetchMetadata.uri(doc.get("_id", URI.class));
		fetchMetadata.statusCode(doc.getInteger("statusCode"));
		// TODO
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
