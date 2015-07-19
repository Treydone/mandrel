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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class WebPage implements Serializable {

	private static final long serialVersionUID = 2015538123437048843L;

	private URL url;

	private Metadata metadata;

	private byte[] body;

	public WebPage(URL url, int statusCode, String statusText, Map<String, List<String>> headers, List<Cookie> cookies, byte[] body) {
		super();
		this.url = url;
		this.metadata = new Metadata();
		metadata.setCookies(cookies);
		metadata.setHeaders(headers);
		metadata.setLastCrawlDate(LocalDateTime.now());
		metadata.setStatusCode(statusCode);
		metadata.setStatusText(statusText);
		metadata.setUrl(url);

		this.body = body;
	}
}
