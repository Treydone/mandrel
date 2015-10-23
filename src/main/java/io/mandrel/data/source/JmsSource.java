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
package io.mandrel.data.source;

import io.mandrel.common.service.TaskContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class JmsSource extends Source {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class JmsSourceDefinition extends SourceDefinition<JmsSource> {
		private static final long serialVersionUID = -4024901085285125948L;

		private String url;

		@Override
		public JmsSource build(TaskContext content) {
			return new JmsSource().url(url);
		}

		@Override
		public String name() {
			return "jms";
		}
	}

	private String url;

	public void register(EntryListener listener) {

	}

	@Override
	public boolean singleton() {
		return false;
	}

	public boolean check() {
		return true;
	}

}
