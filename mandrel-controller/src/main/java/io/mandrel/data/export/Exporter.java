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
package io.mandrel.data.export;

import io.mandrel.blob.Blob;
import io.mandrel.common.loader.NamedDefinition;
import io.mandrel.common.service.ObjectFactory;
import io.mandrel.data.content.FieldExtractor;
import io.mandrel.document.Document;

import java.io.Serializable;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

public interface Exporter {

	public static abstract class ExporterDefinition<EXPORTER extends Exporter> implements ObjectFactory<EXPORTER>, NamedDefinition, Serializable {
		private static final long serialVersionUID = 661004430648193923L;

	}

	String contentType();

	void init(Writer writer) throws Exception;

	void close() throws Exception;

	void export(Collection<Document> documents, List<FieldExtractor> fields) throws Exception;

	void export(Collection<Blob> blobs) throws Exception;
}
