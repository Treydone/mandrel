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

import io.mandrel.metrics.Timeserie.Data;

import java.time.LocalDateTime;
import java.util.TreeSet;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class Timeserie extends TreeSet<Data> {

	private static final long serialVersionUID = -3733297273926957662L;

	@lombok.Data
	@EqualsAndHashCode(exclude = "value")
	@ThriftStruct
	public static class Data implements Comparable<Data> {

		@Getter(onMethod = @__(@ThriftField(1)))
		private final LocalDateTime time;
		@Getter(onMethod = @__(@ThriftField(2)))
		private final Long value;

		@Override
		public int compareTo(Data other) {
			return other.getTime().compareTo(time);
		}

		@ThriftConstructor
		public Data(LocalDateTime time, Long value) {
			this.time = time;
			this.value = value;
		}

		public static Data of(LocalDateTime time, Long value) {
			return new Data(time, value);
		}
	}
}
