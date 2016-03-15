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
package io.mandrel.frontier.revisit;

import io.mandrel.blob.BlobMetadata;
import io.mandrel.common.service.TaskContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class NoRevisitStrategy extends RevisitStrategy {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class NoRevisitStrategyDefinition extends RevisitStrategyDefinition<NoRevisitStrategy> {

		private static final long serialVersionUID = 7795515719836831691L;

		@Override
		public NoRevisitStrategy build(TaskContext content) {
			return new NoRevisitStrategy();
		}

		@Override
		public String name() {
			return "no";
		}
	}

	@Override
	public boolean isScheduledForRevisit(BlobMetadata metadata) {
		return false;
	}
}
