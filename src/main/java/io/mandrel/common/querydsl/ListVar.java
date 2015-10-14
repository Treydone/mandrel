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
package io.mandrel.common.querydsl;

import java.util.ArrayList;
import java.util.List;

import org.parboiled.support.Var;

public class ListVar extends Var<List<String>> {

	public ListVar() {
		this(new ArrayList<>());
	}

	public ListVar(List<String> value) {
		super(value);
	}

	public boolean isEmpty() {
		return get() == null || get().size() == 0;
	}

	public boolean add(String text) {
		if (get() == null) {
			List<String> tmp = new ArrayList<String>();
			tmp.add(text);
			return set(tmp);
		}
		get().add(text);
		return true;
	}

}
