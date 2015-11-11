package io.mandrel.frontier.store.impl;

import java.net.URI;

import io.mandrel.frontier.store.Queue;
import io.mandrel.frontier.store.impl.KafkaFrontierStore.KafkaFrontierStoreDefinition;

import org.junit.Test;

public class KafkaFrontierStoreTest {

	@Test
	public void test() throws InterruptedException {

		KafkaFrontierStoreDefinition definition = new KafkaFrontierStoreDefinition();
		KafkaFrontierStore store = definition.build(null);

		Queue<URI> queue = store.create("queue");
		queue.schedule(URI.create("test://test"));
		Thread.sleep(500);

		queue.pool();
	}
}
