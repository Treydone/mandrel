package io.mandrel.metadata.impl;

import io.mandrel.common.data.Spider;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.metadata.FetchMetadata;
import io.mandrel.metadata.impl.MongoMetadataStore.MongoMetadataStoreDefinition;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.mongodb.client.model.Projections;

public class MongoMetadataStoreTest {

	@Test
	public void test() {

		Spider spider = new Spider().setId(30);
		TaskContext taskContext = new TaskContext();
		taskContext.setDefinition(spider);

		MongoMetadataStoreDefinition definition = new MongoMetadataStoreDefinition();
		MongoMetadataStore store = definition.build(taskContext);

		System.err.println(Sets.newHashSet(store.getCollection().find().projection(Projections.include("_id")).map(doc -> Uri.create(doc.getString("_id")))
				.iterator()));

		Uri uri = new Uri("http", null, "test", 80, "/pouet", null);
		store.delete(uri);
		store.addMetadata(uri, new FetchMetadata());

		Set<Uri> deduplicate = store.deduplicate(Sets.newHashSet(uri));

		Assertions.assertThat(deduplicate).isEmpty();
	}
}
