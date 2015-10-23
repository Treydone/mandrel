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
package io.mandrel.requests.ftp;

import io.mandrel.blob.Blob;
import io.mandrel.common.data.FtpStrategy;
import io.mandrel.common.data.Spider;
import io.mandrel.common.service.TaskContext;
import io.mandrel.requests.Requester;

import java.net.URI;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;

@Slf4j
@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class FtpRequester extends Requester {

	@Data
	public static class FtpRequesterDefinition implements RequesterDefinition {

		private static final long serialVersionUID = -9205125497698919267L;

		@JsonProperty("strategy")
		private FtpStrategy strategy;

		@Override
		public String name() {
			return "ftp";
		}

		@Override
		public Requester build(TaskContext context) {
			return new FtpRequester(context).strategy(strategy);
		}
	}

	public FtpRequester(TaskContext context) {
		super(context);
	}

	private FtpStrategy strategy;

	@Override
	public void init() {
	}

	@Override
	public Blob getBlocking(URI uri, Spider spider) throws Exception {
		return null;
	}

	@Override
	public Blob getBlocking(URI uri) throws Exception {
		return null;
	}

	@Override
	public Set<String> getProtocols() {
		return Sets.newHashSet("ftp", "ftps");
	}

	@Override
	public boolean check() {
		// TODO
		return true;
	}
}
