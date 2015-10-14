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
package io.mandrel.blob;

import io.mandrel.io.ContentMetadata;
import io.mandrel.metadata.FetchMetadata;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;

import javax.annotation.Nullable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = true)
public class BlobMetadata implements Serializable {

	private static final long serialVersionUID = -4923932605977633603L;

	/**
	 * URI used to access this resource
	 */
	private URI uri;

	/**
	 * Name of this resource. Names are dictated by the user. For files, this
	 * may be the filename, ex. file.txt
	 * 
	 */
	private String name;

	@Nullable
	private String container;

	private ContentMetadata contentMetadata;

	/**
	 * Any key-value pairs associated with the fetching of the resource.
	 */
	private FetchMetadata fetchMetadata;

	/**
	 * Creation date of the resource, possibly null.
	 */
	private Date creationDate;

	/**
	 * Last modification time of the resource
	 */
	private Date lastModified;

	/** Size of the resource, possibly null. */
	private Long size;
}
