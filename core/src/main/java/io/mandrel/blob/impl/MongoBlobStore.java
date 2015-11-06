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
package io.mandrel.blob.impl;

import io.mandrel.blob.Blob;
import io.mandrel.blob.BlobMetadata;
import io.mandrel.blob.BlobStore;
import io.mandrel.common.service.TaskContext;
import io.mandrel.io.Payloads;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.netflix.servo.util.Throwables;

public class MongoBlobStore extends BlobStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class MongoBlobStoreDefinition implements BlobStoreDefinition {

		private static final long serialVersionUID = -9205125497698919267L;

		private String uri;
		private String database;
		private String bucket;
		private int batchSize = 10;

		@Override
		public String name() {
			return "mongo";
		}

		@Override
		public MongoBlobStore build(TaskContext context) {
			MongoClientOptions.Builder options = MongoClientOptions.builder();
			// TODO options.description("");
			MongoClientURI uri = new MongoClientURI(this.uri, options);
			MongoClient client = new MongoClient(uri);
			return new MongoBlobStore(context, client, database, bucket, batchSize);
		}
	}

	private final int batchSize;
	private final String databaseName;
	private final MongoClient mongoClient;
	private final GridFSBucket bucket;
	@Getter
	private final ObjectMapper mapper;

	public MongoBlobStore(TaskContext context, MongoClient mongoClient, String databaseName, String bucketName, int batchSize) {
		super(context);
		this.mongoClient = mongoClient;
		this.databaseName = databaseName;
		this.bucket = GridFSBuckets.create(mongoClient.getDatabase(databaseName), bucketName);
		this.batchSize = batchSize;
		this.mapper = new ObjectMapper();
	}

	private Function<? super GridFSDownloadStream, ? extends Blob> fromFile = stream -> {
		Document document = stream.getGridFSFile().getMetadata();
		BlobMetadata metadata;
		try {
			metadata = getMapper().readValue(document.toJson(), BlobMetadata.class);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		return new Blob(metadata).payload(Payloads.newPayload(stream));
	};

	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void init() {

	}

	@Override
	public URI putBlob(URI uri, Blob blob) {
		GridFSUploadOptions options = new GridFSUploadOptions();

		Document document;
		try {
			document = Document.parse(mapper.writeValueAsString(blob.metadata()));
		} catch (JsonProcessingException e) {
			throw Throwables.propagate(e);
		}
		options.metadata(document);

		GridFSUploadStream file = bucket.openUploadStream(uri.toString(), options);
		try {
			IOUtils.copy(blob.payload().openStream(), file);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		file.close();

		return URI.create("mongodb://" + databaseName + "/" + bucket.getBucketName() + "/" + file.getFileId().toString());
	}

	@Override
	public Blob getBlob(URI uri) {
		String id = Iterators.getLast(Splitter.on('/').split(uri.getPath()).iterator());
		GridFSDownloadStream stream = bucket.openDownloadStream(new ObjectId(id));
		return fromFile.apply(stream);
	}

	@Override
	public void deleteAll() {
		bucket.drop();
	}

	@Override
	public void byPages(int pageSize, Callback callback) {
		MongoCursor<GridFSFile> cursor = bucket.find().iterator();
		boolean loop = true;
		try {
			while (loop) {
				List<GridFSFile> files = new ArrayList<>(batchSize);
				int i = 0;
				while (cursor.hasNext() && i < batchSize) {
					files.add(cursor.next());
					i++;
				}
				loop = callback.on(files.stream().map(file -> bucket.openDownloadStream(file.getObjectId())).map(fromFile).collect(Collectors.toList()));
			}
		} finally {
			cursor.close();
		}
	}

	@Override
	public void close() throws IOException {
		mongoClient.close();
	}
}
