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
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true)
@ThriftStruct
public class JobStatus implements Serializable {

	private static final long serialVersionUID = 6432327802272550038L;

	@JsonProperty("status")
	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private JobStatuses status;

	@JsonProperty("created")
	@Getter(onMethod = @__(@ThriftField(2)))
	@Setter(onMethod = @__(@ThriftField))
	private LocalDateTime created;

	@JsonProperty("started")
	@Getter(onMethod = @__(@ThriftField(3)))
	@Setter(onMethod = @__(@ThriftField))
	private LocalDateTime started;

	@JsonProperty("paused")
	@Getter(onMethod = @__(@ThriftField(4)))
	@Setter(onMethod = @__(@ThriftField))
	private LocalDateTime paused;

	@JsonProperty("ended")
	@Getter(onMethod = @__(@ThriftField(5)))
	@Setter(onMethod = @__(@ThriftField))
	private LocalDateTime ended;

	@JsonProperty("killed")
	@Getter(onMethod = @__(@ThriftField(6)))
	@Setter(onMethod = @__(@ThriftField))
	private LocalDateTime killed;

	@JsonProperty("deleted")
	@Getter(onMethod = @__(@ThriftField(7)))
	@Setter(onMethod = @__(@ThriftField))
	private LocalDateTime deleted;

}
