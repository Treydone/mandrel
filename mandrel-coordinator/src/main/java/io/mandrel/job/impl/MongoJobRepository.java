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
package io.mandrel.job.impl;

import io.mandrel.cluster.idgenerator.IdGenerator;
import io.mandrel.cluster.idgenerator.MongoIdGenerator;
import io.mandrel.common.bson.JsonBsonCodec;
import io.mandrel.common.data.Job;
import io.mandrel.common.data.JobStatuses;
import io.mandrel.job.JobRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ConditionalOnProperty(value = "engine.mongodb.enabled", matchIfMissing = true)
public class MongoJobRepository implements JobRepository {

	private final IdGenerator idGenerator = new MongoIdGenerator();
	private final MongoClient client;
	private final MongoProperties properties;
	private final ObjectMapper mapper;

	private MongoCollection<Document> collection;

	@PostConstruct
	public void init() {
		collection = client.getDatabase(properties.getMongoClientDatabase()).getCollection("jobs");
	}

	public Job add(Job job) {
		long id = idGenerator.generateId("jobs");
		job.setId(id);
		collection.insertOne(JsonBsonCodec.toBson(mapper, job));
		return job;
	}

	public Job update(Job job) {
		collection.replaceOne(new Document("_id", job.getId()), JsonBsonCodec.toBson(mapper, job));
		return job;
	}

	public void updateStatus(long jobId, String status) {
		collection.updateOne(new Document("_id", jobId),
				new Document("$set", new Document("status", status).append(status, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())));
	}

	public void delete(long id) {
		collection.deleteOne(Filters.eq("_id", id));
	}

	public Optional<Job> get(long id) {
		Document doc = collection.find(Filters.eq("_id", id)).first();
		return doc == null ? Optional.empty() : Optional.of(JsonBsonCodec.fromBson(mapper, doc, Job.class));
	}

	public List<Job> listActive() {
		Bson filter = activeFilter();
		List<Job> content = Lists.newArrayList(collection.find(filter).map(doc -> JsonBsonCodec.fromBson(mapper, doc, Job.class)));
		return content;
	}

	public List<Job> listLastActive(int size) {
		Bson filter = activeFilter();
		List<Job> content = Lists.newArrayList(collection.find(filter).sort(Sorts.descending("created")).limit(size)
				.map(doc -> JsonBsonCodec.fromBson(mapper, doc, Job.class)));
		return content;
	}

	@Override
	public Page<Job> page(Pageable pageable) {
		List<Job> content = Lists.newArrayList(collection.find().limit(pageable.getPageSize()).skip(pageable.getOffset())
				.map(doc -> JsonBsonCodec.fromBson(mapper, doc, Job.class)));
		return new PageImpl<>(content, pageable, collection.count());
	}

	@Override
	public Page<Job> pageForActive(Pageable pageable) {
		Bson filter = Filters.ne("status", JobStatuses.DELETED);
		List<Job> content = Lists.newArrayList(collection.find(filter).limit(pageable.getPageSize()).skip(pageable.getOffset())
				.map(doc -> JsonBsonCodec.fromBson(mapper, doc, Job.class)));
		return new PageImpl<>(content, pageable, collection.count());
	}

	protected Bson activeFilter() {
		return Filters.or(Filters.eq("status", JobStatuses.STARTED), Filters.eq("status", JobStatuses.PAUSED),
				Filters.eq("status", JobStatuses.INITIATED));
	}
}
