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
import io.mandrel.common.data.Job;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.requests.Requester;

import java.io.IOException;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.google.common.collect.Sets;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class FtpRequester extends Requester {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class FtpRequesterDefinition extends RequesterDefinition<FtpRequester> {

		private static final long serialVersionUID = -9205125497698919267L;

		@Override
		public String name() {
			return "ftp";
		}

		@Override
		public FtpRequester build(TaskContext context) {
			return build(new FtpRequester(context), context);
		}
	}

	public FtpRequester(TaskContext context) {
		super(context);
	}

	public FtpRequester() {
		super(null);
	}

	@Override
	public void init() {
	}

	@Override
	public Blob get(Uri uri, Job job) throws Exception {
		return null;
	}

	@Override
	public Blob get(Uri uri) throws Exception {
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

	@Override
	public void close() throws IOException {

	}
}
