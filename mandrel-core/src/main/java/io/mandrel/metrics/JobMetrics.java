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

import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@Data
@ThriftStruct
public class JobMetrics {

	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private Long nbPagesTotal = Long.valueOf(0);
	@Getter(onMethod = @__(@ThriftField(2)))
	@Setter(onMethod = @__(@ThriftField))
	private Long totalSizeTotal = Long.valueOf(0);
	@Getter(onMethod = @__(@ThriftField(3)))
	@Setter(onMethod = @__(@ThriftField))
	private Long totalTimeToFetch = Long.valueOf(0);

	@Getter(onMethod = @__(@ThriftField(4)))
	@Setter(onMethod = @__(@ThriftField))
	private Long readTimeout;
	@Getter(onMethod = @__(@ThriftField(5)))
	@Setter(onMethod = @__(@ThriftField))
	private Long connectTimeout;
	@Getter(onMethod = @__(@ThriftField(6)))
	@Setter(onMethod = @__(@ThriftField))
	private Long connectException;

	@Getter(onMethod = @__(@ThriftField(7)))
	@Setter(onMethod = @__(@ThriftField))
	private Map<String, Long> extractors;
	@Getter(onMethod = @__(@ThriftField(8)))
	@Setter(onMethod = @__(@ThriftField))
	private Map<String, Long> statuses;
	@Getter(onMethod = @__(@ThriftField(9)))
	@Setter(onMethod = @__(@ThriftField))
	private Map<String, Long> hosts;
	@Getter(onMethod = @__(@ThriftField(10)))
	@Setter(onMethod = @__(@ThriftField))
	private Map<String, Long> contentTypes;
}
