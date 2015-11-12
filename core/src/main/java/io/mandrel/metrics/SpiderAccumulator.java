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
package io.mandrel.metrics;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SpiderAccumulator extends Accumulator {

	private static final String PREFIX = "spider_";

	private final long spiderId;

	public void incNbPages() {
		add(PREFIX + spiderId + ".nbPages", 1);
	}

	public void incTotalSize(long size) {
		add(PREFIX + spiderId + ".totalSize", size);
	}

	public void incPageForStatus(int httpStatus) {
		add(PREFIX + spiderId + ".statuses." + httpStatus, 1);
	}

	public void incPageForHost(String host) {
		add(PREFIX + spiderId + ".hosts." + host, 1);
	}

	public void incPageForContentType(String contentType) {
		add(PREFIX + spiderId + "contentTypes." + contentType, 1);
	}

	public void incDocumentForExtractor(String extractor, int number) {
		add(PREFIX + spiderId + "extractors." + extractor, number);
	}

	public SpiderAccumulator(long spiderId) {
		this.spiderId = spiderId;
	}

	public void incConnectException() {
		add(PREFIX + spiderId + "connectException", 1);
	}

	public void incReadTimeout() {
		add(PREFIX + spiderId + "readTimeout", 1);
	}

	public void incConnectTimeout() {
		add(PREFIX + spiderId + "connectTimeout", 1);
	}

	public void incTotalTimeToFetch(long time) {
		add(PREFIX + spiderId + "totalTimeToFetch", 1);
	}
}
