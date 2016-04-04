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
package io.mandrel.document.impl;

import io.mandrel.common.service.TaskContext;
import io.mandrel.data.content.DataExtractor;
import io.mandrel.document.Document;
import io.mandrel.document.NavigableDocumentStore;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true, fluent = true)
public class ElasticsearchDocumentStore extends NavigableDocumentStore {

	@Data
	@Slf4j
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class ElasticsearchDocumentStoreDefinition extends DocumentStoreDefinition<ElasticsearchDocumentStore> {

		private static final long serialVersionUID = 5439636201136714869L;

		@JsonIgnore
		private Settings.Builder settings = Settings.builder();

		@JsonAnyGetter
		public Map<String, String> any() {
			return settings.internalMap();
		}

		@JsonAnySetter
		public void set(String name, String value) {
			settings.put(name, value);
		}

		@JsonProperty("addresses")
		private List<HostAndPort> addresses = Arrays.asList(HostAndPort.fromParts("localhost", 9300));
		@JsonProperty("cluster")
		private String cluster = "mandrel";
		@JsonProperty("index")
		private String index = "mandrel_{0}";
		@JsonProperty("type")
		private String type = "document";
		@JsonProperty("batch_size")
		private int batchSize = 1000;

		@Override
		public String name() {
			return "elasticsearch";
		}

		@Override
		public ElasticsearchDocumentStore build(TaskContext context) {
			TransportClient client = TransportClient.builder().settings(settings.put("cluster.name").build()).build();
			addresses.forEach(hostAndPort -> {
				try {
					client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostAndPort.getHostText()), hostAndPort
							.getPortOrDefault(9300)));
				} catch (UnknownHostException e) {
					log.error("Unknown host: " + e);
					throw Throwables.propagate(e);
				}
			});
			return new ElasticsearchDocumentStore(context, dataExtractor, client, MessageFormat.format(index, context.getJobId()), type, batchSize);
		}
	}

	private final Client client;
	private final int batchSize;
	private final String index;
	private final String type;

	public ElasticsearchDocumentStore(TaskContext context, DataExtractor metadataExtractor, TransportClient client, String index, String type, int batchSize) {
		super(context, metadataExtractor);
		this.client = client;
		this.index = index;
		this.type = type;
		this.batchSize = batchSize;
	}

	@Override
	public void save(Document data) {
		if (data != null) {
			client.index(Requests.indexRequest(index).type(type).id(data.getId()).source(data)).actionGet();
		}
	}

	@Override
	public void save(List<Document> data) {
		if (data != null) {
			BulkRequest bulkRequest = Requests.bulkRequest();
			data.forEach(item -> {
				bulkRequest.add(Requests.indexRequest(index).type(type).id(item.getId()).source(data));
			});
			client.bulk(bulkRequest).actionGet();
		}
	}

	@Override
	public boolean check() {
		// TODO
		return true;
	}

	@Override
	public void deleteAll() {
		client.admin().indices().delete(Requests.deleteIndexRequest(index)).actionGet();
	}

	@Override
	public void byPages(int pageSize, Callback callback) {

		SearchResponse searchResponse = client.prepareSearch(index).setSize(pageSize).setFrom(0).setScroll(new Scroll(TimeValue.timeValueMinutes(10))).get();
		boolean loop = true;
		try {
			while (loop) {
				loop = callback.on(StreamSupport.stream(searchResponse.getHits().spliterator(), true).map(mapper).collect(Collectors.toList()));
				searchResponse = client.searchScroll(Requests.searchScrollRequest(searchResponse.getScrollId())).actionGet();

				if (searchResponse.getHits().hits() == null) {
					break;
				}
			}
		} finally {
			client.prepareClearScroll().addScrollId(searchResponse.getScrollId()).execute();
		}
	}

	@Override
	public long total() {
		try {
			return client.prepareSearch(index).setSize(0).setFrom(0).setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	private Function<? super SearchHit, ? extends Document> mapper = hit -> {
		Map<String, Object> source = hit.getSource();
		Document document = new Document();
		document.setId(hit.getId());
		document.putAll(source.entrySet().stream().map(entry -> Pair.of(entry.getKey(), (List<Object>) entry.getValue()))
				.collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
		return document;
	};

	@Override
	public Collection<Document> byPages(int pageSize, int pageNumber) {
		return StreamSupport
				.stream(client.prepareSearch(index).setSize(pageSize).setFrom(pageNumber * pageSize).setQuery(QueryBuilders.matchAllQuery()).get().getHits()
						.spliterator(), true).map(mapper).collect(Collectors.toList());
	}

	@Override
	public void init() {
	}

	@Override
	public void close() throws IOException {
		client.close();
	}
}
