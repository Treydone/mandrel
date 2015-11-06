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
package io.mandrel.data.content.selector;

import io.mandrel.blob.BlobMetadata;
import io.mandrel.io.Payload;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

public class JsonSelector extends BodySelector<String> {

	private final static LoadingCache<String, JsonPath> evaluations = CacheBuilder.newBuilder().maximumSize(10000).build(new CacheLoader<String, JsonPath>() {
		@Override
		public JsonPath load(String expression) throws Exception {
			return JsonPath.compile(expression);
		}
	});

	@Override
	public String name() {
		return "jsonpath";
	}

	@Override
	public Instance<String> init(BlobMetadata data, Payload payload, boolean isSegment) {

		Configuration.setDefaults(new Configuration.Defaults() {

			private final JsonProvider jsonProvider = new JacksonJsonProvider();
			private final MappingProvider mappingProvider = new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.of(Option.ALWAYS_RETURN_LIST);
			}
		});

		DocumentContext context;
		try {
			context = JsonPath.parse(payload.openStream());
		} catch (IOException e) {
			payload.release();
			throw Throwables.propagate(e);
		}
		return new JsonSelectorInstance(context);
	}

	public class JsonSelectorInstance implements Instance<String> {

		private final DocumentContext context;

		public JsonSelectorInstance(DocumentContext context) {
			super();
			this.context = context;
		}

		@Override
		public <U> List<U> select(String value, DataConverter<String, U> converter) {
			try {
				List<String> results = context.read(evaluations.get(value), new TypeRef<List<String>>() {
				});
				return results.stream().map(el -> converter.convert(el)).collect(Collectors.toList());
			} catch (ExecutionException e) {
				throw Throwables.propagate(e);
			}
		}
	}
}
