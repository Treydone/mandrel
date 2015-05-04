package io.mandrel.gateway.impl;

import io.mandrel.common.data.Politeness;
import io.mandrel.common.serialization.CompressionType;
import io.mandrel.common.serialization.KryoSerializer;
import io.mandrel.data.spider.Link;
import io.mandrel.http.Metadata;
import io.mandrel.http.WebPage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
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

	private static InternalStore store;

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
				new KryoPool.Builder(factory).softReferences().build(), 0));
		config.getSerializationConfig().setGlobalSerializerConfig(global);

		instance = Hazelcast.newHazelcastInstance(config);
		store = new InternalStore();
		store.setHazelcastInstance(instance);
	}

	@AfterClass
	public static void after() {
		instance.shutdown();
	}

	@Test
	public void addPage() throws MalformedURLException {

		store.addPage(0, "http://wikipedia.org/0", new WebPage(new URL("http://wikipedia.org"), 200, "OK", null, null, "<html></html>".getBytes()));
		store.addPage(0, "http://wikipedia.org/1", new WebPage(new URL("http://wikipedia.org"), 200, "OK", null, null, "<html></html>".getBytes()));
	}

	@Test
	public void addMetadata() throws MalformedURLException {

		store.addMetadata(0, "http://wikipedia.org", new Metadata().setStatusCode(200).setStatusText("OK").setUrl(new URL("http://wikipedia.org")));
	}

	@Test
	public void filter() throws MalformedURLException {
		store.addMetadata(0, "http://toto", new Metadata().setStatusCode(200).setStatusText("OK").setUrl(new URL("http://toto")));
		store.addMetadata(0, "http://toto/2", new Metadata().setStatusCode(200).setStatusText("OK").setUrl(new URL("http://toto/2")));

		Set<Link> filtered = store.filter(0, Sets.newHashSet(new Link().setUri("http://toto")), new Politeness());

		Assertions.assertThat(filtered).contains(new Link().setUri("http://toto/2"));
	}

}
