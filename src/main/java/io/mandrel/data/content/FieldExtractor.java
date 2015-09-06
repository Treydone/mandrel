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
package io.mandrel.data.content;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FieldExtractor extends NamedDataExtractorFormatter {

	private static final long serialVersionUID = 2268103421186155100L;

	@JsonProperty("name")
	private String name;

	@JsonProperty("use_multiple")
	private boolean useMultiple = false;

	@JsonProperty("first_only")
	private boolean firstOnly = true;

	@JsonProperty("extractor")
	private Extractor extractor;

	@JsonProperty("formatter")
	private Formatter formatter;
}
