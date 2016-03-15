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
package io.mandrel.common.data;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Politeness implements Serializable {

	private static final long serialVersionUID = -3487435400772289245L;

	@JsonProperty("page_mean_rate")
	private long pageMeanRate = 20;

	@JsonProperty("page_peek_rate")
	private long pagePeekRate = 50;

	@JsonProperty("max_bandwith")
	private long maxBandwith = 8 * 1024 * 1024;

	@JsonProperty("max_peek_bandwith")
	private long maxPeekBandwith = 20 * 1024 * 1024;

	@JsonProperty("max_uris")
	private long maxUris = 100;

	@JsonProperty("wait")
	private long wait = 100;

}
