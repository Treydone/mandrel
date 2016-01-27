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
///*
// * Licensed to Mandrel under one or more contributor
// * license agreements. See the NOTICE file distributed with
// * this work for additional information regarding copyright
// * ownership. Mandrel licenses this file to you under
// * the Apache License, Version 2.0 (the "License"); you may
// * not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package io.mandrel.document.impl;
//
//import io.mandrel.common.service.TaskContext;
//import io.mandrel.data.content.MetadataExtractor;
//import io.mandrel.document.Document;
//import io.mandrel.document.DocumentStore;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.function.Consumer;
//import java.util.stream.Collectors;
//
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.experimental.Accessors;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lambdaworks.redis.RedisClient;
//import com.lambdaworks.redis.RedisConnectionPool;
//import com.lambdaworks.redis.api.async.RedisAsyncCommands;
//import com.mongodb.client.MongoCursor;
//import com.mongodb.client.model.Filters;
//import com.mongodb.client.model.ReplaceOneModel;
//import com.mongodb.client.model.UpdateOptions;
//
//@Data
//@EqualsAndHashCode(callSuper = false)
//@Accessors(chain = true, fluent = true)
//public class RedisDocumentStore extends DocumentStore {
//
//	@Data
//	@Accessors(chain = false, fluent = false)
//	@EqualsAndHashCode(callSuper = false)
//	public static class RedisDocumentStoreDefinition extends DocumentStoreDefinition<RedisDocumentStore> {
//
//		private static final long serialVersionUID = 4668372774872903452L;
//
//		@JsonProperty("uri")
//		private String uri = "redis://localhost:6379/0";
//
//		@Override
//		public String name() {
//			return "redis";
//		}
//
//		@Override
//		public RedisDocumentStore build(TaskContext context) {
//			return new RedisDocumentStore(context, metadataExtractor, uri);
//		}
//	}
//
//	private final ObjectMapper mapper = new ObjectMapper();
//	private final RedisConnectionPool<RedisAsyncCommands<String, String>> pool;
//
//	public RedisDocumentStore(TaskContext context, MetadataExtractor metadataExtractor, String uri) {
//		super(context, metadataExtractor);
//
//		RedisClient redisClient = RedisClient.create(uri);
//		pool = redisClient.asyncPool(4, 4);
//	}
//
//	public void with(Consumer<? super RedisAsyncCommands<String, String>> action) {
//		RedisAsyncCommands<String, String> conn = pool.allocateConnection();
//		try {
//			action.accept(conn);
//		} finally {
//			pool.freeConnection(conn);
//		}
//	}
//
//	@Override
//	public void save(Document data) {
//		if (data != null) {
//			
//			
//			with(conn -> {
//				try {
//					conn.set(context.getSpiderId() + "." + data.getId(), mapper.writeValueAsString(data));
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			});
//		}
//	}
//
//	@Override
//	public void save(List<Document> data) {
//		if (data != null) {
//			List<ReplaceOneModel<org.bson.Document>> updates = data.stream().map(toBson)
//					.map(doc -> new ReplaceOneModel<org.bson.Document>(Filters.eq("_id", doc.getString("_id")), doc, new UpdateOptions().upsert(true)))
//					.collect(Collectors.toList());
//			collection.bulkWrite(updates);
//		}
//	}
//
//	@Override
//	public boolean check() {
//		// TODO
//		return true;
//	}
//
//	@Override
//	public void deleteAll() {
//		collection.drop();
//	}
//
//	@Override
//	public void byPages(int pageSize, Callback callback) {
//		MongoCursor<org.bson.Document> cursor = collection.find().iterator();
//		boolean loop = true;
//		try {
//			while (loop) {
//				List<org.bson.Document> docs = new ArrayList<>(batchSize);
//				int i = 0;
//				while (cursor.hasNext() && i < batchSize) {
//					docs.add(cursor.next());
//					i++;
//				}
//				loop = callback.on(docs.stream().map(fromBson).collect(Collectors.toList()));
//			}
//		} finally {
//			cursor.close();
//		}
//	}
//
//	@Override
//	public long total() {
//		return collection.count();
//	}
//
//	@Override
//	public Collection<Document> byPages(int pageSize, int pageNumber) {
//		MongoCursor<org.bson.Document> cursor = collection.find().skip(pageSize * pageNumber).limit(pageSize).iterator();
//		List<org.bson.Document> docs = new ArrayList<>(10);
//		while (cursor.hasNext()) {
//			docs.add(cursor.next());
//		}
//		return docs.stream().map(fromBson).collect(Collectors.toList());
//	}
//
//	@Override
//	public void init() {
//	}
//
//	@Override
//	public void close() throws IOException {
//		mongoClient.close();
//	}
//}
