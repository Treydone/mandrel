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
package io.mandrel.frontier.store;

import io.mandrel.common.service.TaskContext;
import io.mandrel.common.unit.ByteSizeValue;

import java.net.URI;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.bson.Document;

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.model.CreateCollectionOptions;

public class MongoFrontierStore extends FrontierStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class MongoFrontierStoreDefinition extends FrontierStoreDefinition<MongoFrontierStore> {
		private static final long serialVersionUID = -5715057009212205361L;

		private String uri;
		private String database;
		private String collectionPrefix = "queue_";
		private ByteSizeValue maxSize = ByteSizeValue.parseBytesSizeValue("200mb");

		@Override
		public String name() {
			return "mongo";
		}

		@Override
		public MongoFrontierStore build(TaskContext context) {
			MongoClientOptions.Builder options = MongoClientOptions.builder();
			// TODO options.description("");
			MongoClientURI uri = new MongoClientURI(this.uri, options);
			return new MongoFrontierStore(context, new MongoClient(uri), database, collectionPrefix, maxSize);
		}
	}

	private final MongoClient mongoClient;
	private final String collectionPrefix;
	private final String databaseName;
	private final ByteSizeValue maxSize;

	public MongoFrontierStore(TaskContext context, MongoClient mongoClient, String databaseName, String collectionPrefix, ByteSizeValue maxSize) {
		super(context);
		this.mongoClient = mongoClient;
		this.collectionPrefix = collectionPrefix;
		this.databaseName = databaseName;
		this.maxSize = maxSize;
	}

	@Override
	public Queue<URI> create(String name) {

		List<String> collections = Lists.newArrayList(mongoClient.getDatabase(databaseName).listCollectionNames());
		String queueName = collectionPrefix + context.getSpiderId() + "_" + name;

		if (collections.contains(queueName)) {
			mongoClient.getDatabase(databaseName).runCommand(new Document("convertToCapped", queueName).append("size", maxSize.bytes()));
		} else {
			mongoClient.getDatabase(databaseName).createCollection(queueName, new CreateCollectionOptions().capped(true).sizeInBytes(maxSize.bytes()));
		}

		return null;
	}

	@Override
	public void finish(URI uri) {

	}

	@Override
	public void delete(URI uri) {

	}

	@Data
	public static class MongoQueue<T> implements Queue<T> {

		private final String name;

		@Override
		public T pool() {
			// .find( { indexedField: { $gt: <lastvalue> } } )
			// {'update' : {'$set' : { 'inProg' : true, 't' : new Date() } } }
			return null;
		}

		@Override
		public void schedule(T item) {
		}

		@Override
		public void schedule(Set<T> items) {
		}
	}

	@Override
	public boolean check() {
		return true;
	}
}
