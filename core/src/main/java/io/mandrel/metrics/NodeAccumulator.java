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
public class NodeAccumulator extends Accumulator {

	private String id;

	public void incNbPages() {
		add(MetricKeys.nodeNbPages(id), 1);
	}

	public void incTotalSize(long size) {
		add(MetricKeys.nodeTotalSize(id), size);
	}

	public void incPageForStatus(int httpStatus) {
		add(MetricKeys.nodePageForStatus(id, httpStatus), 1);
	}

	public void incPageForHost(String host) {
		add(MetricKeys.nodePageForHost(id, host), 1);
	}

	public void incPageForContentType(String contentType) {
		add(MetricKeys.nodePageForContentType(id, contentType), 1);
	}
}
