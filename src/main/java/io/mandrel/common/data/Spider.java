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
import io.mandrel.data.source.Source.SourceDefinition;
import io.mandrel.frontier.Frontier;
import io.mandrel.frontier.Frontier.FrontierDefinition;
import io.mandrel.frontier.SimpleFrontier.SimpleFrontierDefinition;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true)
public class Spider implements Serializable {

	private static final long serialVersionUID = 7577967853566572778L;

	@JsonProperty("_id")
	private long id;
	
	@JsonProperty("version")
	private long version;

	@JsonProperty("name")
	private String name;

	@JsonProperty("status")
	private State state = State.NEW;

	@JsonProperty("added")
	private LocalDateTime added;

	@JsonProperty("started")
	private LocalDateTime started;

	@JsonProperty("ended")
	private LocalDateTime ended;

	@JsonProperty("cancelled")
	private LocalDateTime cancelled;

	@JsonProperty("sources")
	private List<SourceDefinition<? extends Source>> sources = new ArrayList<>();

	@JsonProperty("filters")
	private Filters filters = new Filters();

	@JsonProperty("extractors")
	private Extractors extractors = new Extractors();

	@JsonProperty("stores")
	private StoresDefinition stores = new StoresDefinition();

	@JsonProperty("frontier")
	private FrontierDefinition<? extends Frontier> frontier = new SimpleFrontierDefinition();

	@JsonProperty("client")
	private Client client = new Client();
}
