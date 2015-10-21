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
import io.mandrel.monitor.health.Checkable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public abstract class DocumentStore extends TaskContextAware implements Checkable, Initializable {

	public DocumentStore(TaskContext context) {
		super(context);
	}

	public interface DocumentStoreDefinition extends NamedDefinition, ObjectFactory<DocumentStore>, Serializable {

	}

	public abstract void save(Document document);

	public abstract void save(List<Document> documents);

	public abstract void deleteAll();

	// Stream<Document> all(long spiderId);

	@FunctionalInterface
	public static interface Callback {
		boolean on(Collection<Document> elements);
	}

	public abstract void byPages(int pageSize, Callback callback);

	public abstract Collection<Document> byPages(int pageSize, int pageNumber);

	public abstract long total();

}
