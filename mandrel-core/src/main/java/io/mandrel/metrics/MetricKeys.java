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

public interface MetricKeys {

	String METRIC_DELIM = ".";
	String TYPE_DELIM = "_";

	String GLOBAL = "global";
	String NODE = "node";
	String SPIDER = "job";

	String NB_PAGES_TOTAL = "nbPagesTotal";
	String TOTAL_SIZE_TOTAL = "totalSizeTotal";
	String STATUSES = "statuses";
	String HOSTS = "hosts";
	String CONTENT_TYPES = "contentTypes";
	String EXTRACTORS = "extractors";

	// GLOBAL
	public static String global() {
		return GLOBAL;
	}

	public static String globalNbPages() {
		return GLOBAL + METRIC_DELIM + NB_PAGES_TOTAL;
	}

	public static String globalTotalSize() {
		return GLOBAL + METRIC_DELIM + TOTAL_SIZE_TOTAL;
	}

	public static String globalPageForStatus(int httpStatus) {
		return GLOBAL + METRIC_DELIM + STATUSES + METRIC_DELIM + httpStatus;
	}

	public static String globalPageForHost(String host) {
		return GLOBAL + METRIC_DELIM + HOSTS + METRIC_DELIM + host;
	}

	public static String globalPageForContentType(String contentType) {
		return GLOBAL + METRIC_DELIM + CONTENT_TYPES + METRIC_DELIM + contentType;
	}

	// NODE
	public static String node(String node) {
		return NODE + TYPE_DELIM + node;
	}

	public static String nodeNbPages(String node) {
		return NODE + TYPE_DELIM + node + METRIC_DELIM + NB_PAGES_TOTAL;
	}

	public static String nodeTotalSize(String node) {
		return NODE + TYPE_DELIM + node + METRIC_DELIM + TOTAL_SIZE_TOTAL;
	}

	public static String nodePageForStatus(String node, int httpStatus) {
		return NODE + TYPE_DELIM + node + METRIC_DELIM + STATUSES + METRIC_DELIM + httpStatus;
	}

	public static String nodePageForHost(String node, String host) {
		return NODE + TYPE_DELIM + node + METRIC_DELIM + HOSTS + METRIC_DELIM + host;
	}

	public static String nodePageForContentType(String node, String contentType) {
		return NODE + TYPE_DELIM + node + METRIC_DELIM + CONTENT_TYPES + METRIC_DELIM + contentType;
	}

	// SPIDER
	public static String job(long jobId) {
		return SPIDER + TYPE_DELIM + jobId;
	}

	public static String jobNbPages(long jobId) {
		return SPIDER + TYPE_DELIM + jobId + METRIC_DELIM + NB_PAGES_TOTAL;
	}

	public static String jobTotalSize(long jobId) {
		return SPIDER + TYPE_DELIM + jobId + METRIC_DELIM + TOTAL_SIZE_TOTAL;
	}

	public static String jobPageForStatus(long jobId, int httpStatus) {
		return SPIDER + TYPE_DELIM + jobId + METRIC_DELIM + STATUSES + METRIC_DELIM + httpStatus;
	}

	public static String jobPageForHost(long jobId, String host) {
		return SPIDER + TYPE_DELIM + jobId + METRIC_DELIM + HOSTS + METRIC_DELIM + host;
	}

	public static String jobPageForContentType(long jobId, String contentType) {
		return SPIDER + TYPE_DELIM + jobId + METRIC_DELIM + CONTENT_TYPES + METRIC_DELIM + contentType;
	}

	public static String jobPageForExtractor(long jobId, String extractor) {
		return SPIDER + TYPE_DELIM + jobId + METRIC_DELIM + EXTRACTORS + METRIC_DELIM + extractor;
	}
}
