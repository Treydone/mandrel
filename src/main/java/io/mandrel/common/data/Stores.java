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
package io.mandrel.common.data;

import io.mandrel.blob.BlobStore;
import io.mandrel.blob.impl.BlobInternalStore;
import io.mandrel.metadata.MetadataStore;
import io.mandrel.metadata.impl.MetadataInternalStore;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Stores implements Serializable {

	private static final long serialVersionUID = -6386148207535019331L;

	@JsonProperty("metadata")
	private MetadataStore metadataStore = new MetadataInternalStore();

	@JsonProperty("blob")
	private BlobStore blobStore = new BlobInternalStore();
}