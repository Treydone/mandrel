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
package io.mandrel.transport;

import io.mandrel.common.MandrelException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class RemoteException extends MandrelException {
	private static final long serialVersionUID = 2420782417683971997L;

	@Getter(onMethod = @__(@ThriftField(0)))
	@Setter(onMethod = @__(@ThriftField))
	private Error error;
	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private String details;

	public static enum Error {

		// Global
		G_UNKNOWN,

		// Definition
		D_DEFINITION_INVALID,

		// Frontier
		// F_TIMEOUT_WHEN_GETTING_URI
	}

	@Override
	public String getMessage() {
		return error.toString() + ": " + details;
	}
}
