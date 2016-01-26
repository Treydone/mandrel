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

import static io.mandrel.metrics.MetricKeys.METRIC_DELIM;
import static io.mandrel.metrics.MetricKeys.SPIDER;
import static io.mandrel.metrics.MetricKeys.TYPE_DELIM;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SpiderAccumulator extends Accumulator {

	private final long spiderId;

	public void incNbPages() {
		add(MetricKeys.spiderNbPages(spiderId), 1);
	}

	public void incTotalSize(long size) {
		add(MetricKeys.spiderTotalSize(spiderId), size);
	}

	public void incPageForStatus(int httpStatus) {
		add(MetricKeys.spiderPageForStatus(spiderId, httpStatus), 1);
	}

	public void incPageForHost(String host) {
		add(MetricKeys.spiderPageForHost(spiderId, host), 1);
	}

	public void incPageForContentType(String contentType) {
		add(MetricKeys.spiderPageForContentType(spiderId, contentType), 1);
	}

	public void incDocumentForExtractor(String extractor, int number) {
		add(MetricKeys.spiderPageForExtractor(spiderId, extractor), number);
	}

	public void incConnectException() {
		add(SPIDER + TYPE_DELIM + spiderId + METRIC_DELIM + "connectException", 1);
	}

	public void incReadTimeout() {
		add(SPIDER + TYPE_DELIM + spiderId + METRIC_DELIM + "readTimeout", 1);
	}

	public void incConnectTimeout() {
		add(SPIDER + TYPE_DELIM + spiderId + METRIC_DELIM + "connectTimeout", 1);
	}

	public void incTotalTimeToFetch(long time) {
		add(SPIDER + TYPE_DELIM + spiderId + METRIC_DELIM + "totalTimeToFetch", 1);
	}
}
