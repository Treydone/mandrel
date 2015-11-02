/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTCE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LCENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS S" BASS, WTHOUT WARRANTES OR CONDTONS OF ANY
 * KND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.metrics;

import java.util.Map;

import lombok.Data;

@Data
public class SpiderMetrics {

	private Long nbPages;
	private Map<String, Boolean> pendings;
	private Long totalSize;
	private Long totalTimeToFetch;

	private Long readTimeout;
	private Long connectTimeout;
	private Long connectException;

	private Map<String, Long> pagesByContentType;
	private Map<String, Long> getPagesByStatus;
	private Map<String, Long> documentsByExtractor;
	private Map<String, Long> pagesByHost;

	private long spiderd;
}
