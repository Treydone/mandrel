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
package io.mandrel.config;

import io.mandrel.blob.BlobStore;
import io.mandrel.blob.impl.BlobInternalStore;
import io.mandrel.data.spider.Link;
import io.mandrel.document.Document;
import io.mandrel.document.DocumentStore;
import io.mandrel.document.impl.InternalDocumentStore;
import io.mandrel.metadata.FetchMetadata;
import io.mandrel.metadata.MetadataStore;
import io.mandrel.requests.ftp.FtpFetchMetadata;
import io.mandrel.requests.http.HttpFetchMetadata;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

@Configuration
public class SerializationConfiguration {

	// TODO Find another way...
	private final static Class<?>[] clazzes = { FetchMetadata.class, HttpFetchMetadata.class, FtpFetchMetadata.class, Link.class,
			//
			Document.class, DocumentStore.class, BlobStore.class, MetadataStore.class, InternalDocumentStore.class, BlobInternalStore.class
	//

	};

	private KryoFactory factory = new KryoFactory() {
		public Kryo create() {
			Kryo kryo = new Kryo();

			// configure kryo instance, customize settings
			for (int i = 0; i < clazzes.length; i++) {
				kryo.register(clazzes[i], i);
			}

			return kryo;
		}
	};

	@Bean
	public KryoPool kryoPool() {
		return new KryoPool.Builder(factory).softReferences().build();
	}
}
