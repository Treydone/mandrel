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
package io.mandrel.metadata;

import io.mandrel.data.spider.Link;

import java.io.Serializable;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true, fluent = true)
public class FetchMetadata implements Serializable {

	private static final long serialVersionUID = 4900693151305950280L;

	private URI uri;

	private int statusCode;
	private String statusText;
	private LocalDateTime lastCrawlDate;
	private long timeToFetch;
	private Set<Link> outlinks;
	private int iteration;

	private URI blob;
}
