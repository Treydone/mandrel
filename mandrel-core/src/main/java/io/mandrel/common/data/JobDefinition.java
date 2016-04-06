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
import io.mandrel.frontier.Frontier.FrontierDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true)
public class JobDefinition implements Serializable {

	private static final long serialVersionUID = -9177969832575860175L;

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("sources")
	private List<SourceDefinition<? extends Source>> sources = new ArrayList<>();

	@JsonProperty("filters")
	private Filters filters = new Filters();

	@JsonProperty("extractors")
	private Extractors extractors = new Extractors();

	@JsonProperty("stores")
	private StoresDefinition stores = new StoresDefinition();

	@JsonProperty("frontier")
	private FrontierDefinition frontier = new FrontierDefinition();

	@JsonProperty("client")
	private Client client = new Client();

	@JsonProperty("politeness")
	private Politeness politeness = new Politeness();
}
