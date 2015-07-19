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

import io.mandrel.data.source.Source;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true)
public class Spider implements Serializable {

	private static final long serialVersionUID = 7577967853566572778L;

	@JsonProperty("id")
	private long id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("status")
	private State state = State.NEW;

	@JsonProperty("sources")
	private List<Source> sources = new ArrayList<>();

	@JsonProperty("filters")
	private Filters filters = new Filters();

	@JsonProperty("extractors")
	private Extractors extractors = new Extractors();

	@JsonProperty("stores")
	private Stores stores = new Stores();

	@JsonProperty("client")
	private Client client = new Client();
}
