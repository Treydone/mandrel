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
package io.mandrel.gateway.impl;

import io.mandrel.blob.impl.BlobInternalStore;
import io.mandrel.common.serialization.CompressionType;
import io.mandrel.common.serialization.KryoSerializer;
import io.mandrel.data.spider.Link;
import io.mandrel.frontier.Politeness;
import io.mandrel.metadata.FetchMetadata;
import io.mandrel.requests.WebPage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.collect.Sets;
import com.hazelcast.config.Config;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class InternalStoreTest {

	private static HazelcastInstance instance;

	private static BlobInternalStore store;

	@BeforeClass
	public static void before() {
		Config config = new Config();
		config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
		config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);

		KryoFactory factory = new KryoFactory() {
			public Kryo create() {
				Kryo kryo = new Kryo();
				return kryo;
			}
		};

		GlobalSerializerConfig global = new GlobalSerializerConfig().setImplementation(new KryoSerializer<>(CompressionType.SNAPPY, Object.class,
				new KryoPool.Builder(factory).softReferences().build(), 100));
		config.getSerializationConfig().setGlobalSerializerConfig(global);

		instance = Hazelcast.newHazelcastInstance(config);
		store = new BlobInternalStore();
		store.setHazelcastInstance(instance);
	}

	@AfterClass
	public static void after() {
		instance.shutdown();
	}

	@Before
	public void beforeEachTest() {
		instance.getMap("pagestore-" + 0).clear();
		instance.getMap("pagemetastore-" + 0).clear();
	}

	@Test
	public void addPage() throws MalformedURLException {

		store.addPage(0, "http://wikipedia.org/0", new WebPage(new URL("http://wikipedia.org/0"), 200, "OK", null, null, "<html></html>".getBytes()));
		store.addPage(0, "http://wikipedia.org/1", new WebPage(new URL("http://wikipedia.org/1"), 200, "OK", null, null, "<html></html>".getBytes()));

		Assertions.assertThat(instance.getMap("pagestore-" + 0).get("http://wikipedia.org/0")).isNotNull();
		Assertions.assertThat(instance.getMap("pagestore-" + 0).get("http://wikipedia.org/1")).isNotNull();
	}

	@Test
	public void getPage() throws MalformedURLException {

		WebPage webPage = new WebPage(new URL("http://wikipedia.org/0"), 200, "OK", null, null, "<html></html>".getBytes());
		store.addPage(0, "http://wikipedia.org/0", webPage);

		WebPage result = store.getBag(0, "http://wikipedia.org/0");

		Assertions.assertThat(result).isEqualTo(webPage);
	}

	@Test
	public void all() throws MalformedURLException {

		WebPage webPage1 = new WebPage(new URL("http://wikipedia.org/1"), 200, "OK", null, null, "<html></html>".getBytes());
		store.addPage(0, "http://wikipedia.org/1", webPage1);

		WebPage webPage2 = new WebPage(new URL("http://wikipedia.org/2"), 200, "OK", null, null, "<html></html>".getBytes());
		store.addPage(0, "http://wikipedia.org/2", webPage2);

		List<WebPage> results = new ArrayList<>();

		store.byPages(0L, 1000, data -> {
			results.addAll(data);
			return CollectionUtils.isNotEmpty(data);
		});

		Assertions.assertThat(results).usingFieldByFieldElementComparator().containsExactly(webPage1, webPage2);
	}

	@Test
	public void delete() throws MalformedURLException {
		store.addMetadata(0, "http://wikipedia.org/0", new FetchMetadata().setStatusCode(200).setStatusText("OK").setUrl(new URL("http://wikipedia.org/0")));
		store.addPage(0, "http://wikipedia.org/0", new WebPage(new URL("http://wikipedia.org/0"), 200, "OK", null, null, "<html></html>".getBytes()));

		Assertions.assertThat(instance.getMap("pagemetastore-" + 0).get("http://wikipedia.org/0")).isNotNull();
		Assertions.assertThat(instance.getMap("pagestore-" + 0).get("http://wikipedia.org/0")).isNotNull();

		store.deleteAllFor(0);

		Assertions.assertThat(instance.getMap("pagemetastore-" + 0).get("http://wikipedia.org/0")).isNull();
		Assertions.assertThat(instance.getMap("pagestore-" + 0).get("http://wikipedia.org/0")).isNull();
	}

	@Test
	public void addMetadata() throws MalformedURLException {

		store.addMetadata(0, "http://wikipedia.org", new FetchMetadata().setStatusCode(200).setStatusText("OK").setUrl(new URL("http://wikipedia.org")));
	}

	@Test
	public void getMetadata() throws MalformedURLException {

		FetchMetadata metdata = new FetchMetadata().setStatusCode(200).setStatusText("OK").setUrl(new URL("http://wikipedia.org"));
		store.addMetadata(0, "http://wikipedia.org", metdata);

		FetchMetadata result = store.getMetadata(0, "http://wikipedia.org");

		Assertions.assertThat(result).isEqualTo(metdata);
	}

	@Test
	public void filter_simple() throws MalformedURLException {
		store.addMetadata(0, "http://toto", new FetchMetadata().setStatusCode(200).setStatusText("OK").setUrl(new URL("http://toto")));

		Set<String> filtered = store.filter(0, Sets.newHashSet(new Link().setUri("http://toto"), new Link().setUri("http://toto/2")), new Politeness());

		Assertions.assertThat(filtered).contains("http://toto/2");
	}

}
