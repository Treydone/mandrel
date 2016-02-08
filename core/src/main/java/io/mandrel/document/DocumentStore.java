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
package io.mandrel.document;

import io.mandrel.common.lifecycle.Initializable;
import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.service.ObjectFactory;
import io.mandrel.common.service.TaskContext;
import io.mandrel.common.service.TaskContextAware;
import io.mandrel.data.content.DataExtractor;
import io.mandrel.monitor.health.Checkable;

import java.io.Closeable;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class DocumentStore extends TaskContextAware implements Checkable, Initializable, Closeable {

	protected final DataExtractor metadataExtractor;

	public DocumentStore(TaskContext context, DataExtractor metadataExtractor) {
		super(context);
		this.metadataExtractor = metadataExtractor;
	}

	public static abstract class DocumentStoreDefinition<DOCUMENTSTORE extends DocumentStore> implements NamedDefinition, ObjectFactory<DOCUMENTSTORE>,
			Serializable {
		private static final long serialVersionUID = -9187921401073694191L;

		@JsonIgnore
		protected DataExtractor dataExtractor;

		public DocumentStoreDefinition<DOCUMENTSTORE> metadataExtractor(DataExtractor metadataExtractor) {
			this.dataExtractor = metadataExtractor;
			return this;
		}
	}

	public abstract void save(Document document);

	public abstract void save(List<Document> documents);

	public boolean isNavigable() {
		return false;
	}
}
