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
package io.mandrel.http;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Metadata implements Serializable {

	private static final long serialVersionUID = -7319512873380929621L;

	private URL url;
	private int statusCode;
	private String statusText;
	private Map<String, List<String>> headers;
	private List<Cookie> cookies;
	private LocalDateTime lastCrawlDate;
	private long timeToFetch;
	private Set<String> outlinks;
}