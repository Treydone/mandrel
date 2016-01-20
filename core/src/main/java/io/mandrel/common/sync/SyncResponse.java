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
package io.mandrel.common.sync;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.collections.CollectionUtils;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@Accessors(chain = true)
@ThriftStruct
public class SyncResponse {

	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private List<Long> created;
	@Getter(onMethod = @__(@ThriftField(2)))
	@Setter(onMethod = @__(@ThriftField))
	private List<Long> updated;
	@Getter(onMethod = @__(@ThriftField(3)))
	@Setter(onMethod = @__(@ThriftField))
	private List<Long> killed;
	@Getter(onMethod = @__(@ThriftField(4)))
	@Setter(onMethod = @__(@ThriftField))
	private List<Long> started;
	@Getter(onMethod = @__(@ThriftField(5)))
	@Setter(onMethod = @__(@ThriftField))
	private List<Long> paused;

	public boolean anyAction() {
		return CollectionUtils.isNotEmpty(created) && CollectionUtils.isNotEmpty(updated) && CollectionUtils.isNotEmpty(killed)
				&& CollectionUtils.isNotEmpty(started) && CollectionUtils.isNotEmpty(paused);
	}
}
