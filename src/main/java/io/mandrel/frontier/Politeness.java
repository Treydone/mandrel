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
package io.mandrel.frontier;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Politeness implements Serializable {

	private static final long serialVersionUID = -3487435400772289245L;

	@JsonProperty("global_rate")
	private long globalRate = 1000;

	@JsonProperty("per_node_rate")
	private long perNodeRate = 500;

	@JsonProperty("max_pages")
	private long maxPages = 100;

	@JsonProperty("wait")
	private long wait = 100;

	@JsonProperty("ignore_robots_txt")
	private boolean ignoreRobotsTxt = false;

	@JsonProperty("recrawl_after")
	private int recrawlAfterSeconds = -1;

}
