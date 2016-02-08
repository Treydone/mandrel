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
package io.mandrel.data.content;

import io.mandrel.blob.Blob;
import io.mandrel.common.data.Filters;
import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.document.Document;
import io.mandrel.document.DocumentStore;
import io.mandrel.document.DocumentStore.DocumentStoreDefinition;
import io.mandrel.document.impl.MongoDocumentStore.MongoDocumentStoreDefinition;
import io.mandrel.script.ScriptingService;

import java.util.List;
import java.util.Map;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public abstract class DataExtractor implements NamedDefinition {

	@JsonProperty("name")
	protected String name;

	@JsonProperty("store")
	protected DocumentStoreDefinition<? extends DocumentStore> documentStore = new MongoDocumentStoreDefinition();

	@JsonProperty("filters")
	protected Filters filters = new Filters();

	public abstract List<Document> extract(ScriptingService engine, Map<String, Instance<?>> selectors, Blob blob);

}
