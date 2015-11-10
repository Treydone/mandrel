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

import io.mandrel.common.service.TaskContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
public class FtpStrategy extends Strategy {

	public FtpStrategy(TaskContext context) {
		super(context);
	}

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class FtpStrategyDefinition extends StrategyDefinition<FtpStrategy> {

		private static final long serialVersionUID = 252540570641044928L;

		@Override
		public String name() {
			return "ftp";
		}

		@Override
		public FtpStrategy build(TaskContext context) {
			return build(new FtpStrategy(context), context);
		}
	}
}
