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
package io.mandrel.metadata.impl;

import io.mandrel.blob.BlobMetadata;
import io.mandrel.common.data.Job;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.metadata.impl.MongoMetadataStore.MongoMetadataStoreDefinition;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.mongodb.client.model.Projections;

public class MongoMetadataStoreTest {

	@Test
	public void test() {

		Job job = new Job().setId(30);
		TaskContext taskContext = new TaskContext();
		taskContext.setDefinition(job);

		MongoMetadataStoreDefinition definition = new MongoMetadataStoreDefinition();
		MongoMetadataStore store = definition.build(taskContext);

		System.err.println(Sets.newHashSet(store.getCollection().find().projection(Projections.include("_id")).map(doc -> Uri.create(doc.getString("_id")))
				.iterator()));

		Uri uri = new Uri("http", null, "test", 80, "/pouet", null);
		store.delete(uri);
		store.addMetadata(uri, new BlobMetadata());

		Set<Uri> deduplicate = store.deduplicate(Sets.newHashSet(uri));

		Assertions.assertThat(deduplicate).isEmpty();
	}
}
