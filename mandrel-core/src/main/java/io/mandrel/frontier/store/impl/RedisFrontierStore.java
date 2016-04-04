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
package io.mandrel.frontier.store.impl;

import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.frontier.store.FetchRequest;
import io.mandrel.frontier.store.FrontierStore;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnectionPool;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;

@Slf4j
public class RedisFrontierStore extends FrontierStore {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class RedisFrontierStoreDefinition extends FrontierStoreDefinition<RedisFrontierStore> {
		private static final long serialVersionUID = -5715057009212205361L;

		@JsonProperty("uri")
		private String uri = "redis://localhost:6379/0";

		@Override
		public String name() {
			return "redis";
		}

		@Override
		public RedisFrontierStore build(TaskContext context) {
			return new RedisFrontierStore(name(), context, uri);
		}
	}

	private final String name;
	private final RedisConnectionPool<RedisAsyncCommands<String, String>> pool;

	public RedisFrontierStore(String name, TaskContext context, String uri) {
		super(context);
		this.name = name;

		RedisClient redisClient = RedisClient.create(uri);
		pool = redisClient.asyncPool(4, 4);
	}

	public <R> R map(Function<? super RedisAsyncCommands<String, String>, ? extends R> mapper) {
		RedisAsyncCommands<String, String> conn = pool.allocateConnection();
		try {
			return mapper.apply(conn);
		} finally {
			pool.freeConnection(conn);
		}
	}

	public void with(Consumer<? super RedisAsyncCommands<String, String>> action) {
		RedisAsyncCommands<String, String> conn = pool.allocateConnection();
		try {
			action.accept(conn);
		} finally {
			pool.freeConnection(conn);
		}
	}

	public void pool(FetchRequest request) {
		map(conn -> conn.rpop(getTopicName(request.getTopic()))).thenAccept(uri -> request.getCallback().on(Uri.create(uri), name));
	}

	public void schedule(String name, Uri item) {
		with(conn -> conn.lpush(getTopicName(name), item.toString()));
	}

	public void schedule(String name, Set<Uri> items) {
		with(conn -> conn.lpush(getTopicName(name), items.stream().map(item -> item.toString()).toArray(String[]::new)));
	}

	private String getTopicName(String name) {
		return "topic_" + context.getJobId() + "_" + name;
	}

	@Override
	public void destroy(String name) {
		with(conn -> conn.del(getTopicName(name)));
	}

	@Override
	public void create(String name) {
	}

	public void close() {
		try {
			if (pool != null)
				pool.close();
		} catch (Exception e) {
			log.info("", e);
		}
	}

	@Override
	public boolean check() {
		// TODO
		return true;
	}
}
