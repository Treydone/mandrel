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
import io.mandrel.blob.BlobMetadata;
import io.mandrel.common.data.Job;
import io.mandrel.common.net.Uri;
import io.mandrel.common.service.TaskContext;
import io.mandrel.requests.Requester;
import io.mandrel.requests.ftp.PooledFtpClient.FtpClientConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;

@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = false)
public class FtpRequester extends Requester {

	@Data
	@Accessors(chain = false, fluent = false)
	@EqualsAndHashCode(callSuper = false)
	public static class FtpRequesterDefinition extends RequesterDefinition<FtpRequester> {

		private static final long serialVersionUID = -9205125497698919267L;

		@JsonProperty("username")
		private String username;

		@JsonProperty("password")
		private String password;

		@JsonProperty("file_type")
		private int fileType = FTP.BINARY_FILE_TYPE;

		@Override
		public String name() {
			return "ftp";
		}

		@Override
		public FtpRequester build(TaskContext context) {
			return build(new FtpRequester(context, new FtpClientConfiguration().password(password).username(username).fileType(fileType)), context);
		}
	}

	private PooledFtpClient ftpClientPool;
	private final FtpClientConfiguration configuration;

	public FtpRequester(TaskContext context, FtpClientConfiguration configuration) {
		super(context);
		this.configuration = configuration;
	}

	public FtpRequester() {
		super(null);
		configuration = new FtpClientConfiguration();
	}

	@Override
	public void init() {
		ftpClientPool = new PooledFtpClient(configuration);
	}

	@Override
	public Blob get(Uri uri, Job job) throws Exception {
		HostAndPort hostAndPort = HostAndPort.fromParts(uri.getHost(), uri.getPort());
		FTPClient ftpClient = ftpClientPool.getResource(hostAndPort);

		// TODO user info?

		try {
			FTPFile file = ftpClient.mlistFile(uri.getPath());

			FtpFetchMetadata metadata = new FtpFetchMetadata();
			metadata.setUri(uri).setStatusCode(ftpClient.getReplyCode()).setStatusText(ftpClient.getReplyString());

			if (file.isFile()) {
				InputStream stream = ftpClient.retrieveFileStream(uri.getPath());
				Blob blob = new Blob(new BlobMetadata().setUri(uri).setSize(file.getSize() < 0 ? null : file.getSize()).setFetchMetadata(metadata))
						.payload(IOUtils.toByteArray(stream));
				return blob;
			}
			if (file.isDirectory()) {

				FTPListParseEngine engine = ftpClient.initiateListParsing(uri.getPath());
				
				// TODO Well, surely, can be better: Payload can be a set of URIs? 
				StringBuilder builder = new StringBuilder();
				while (engine.hasNext()) {
					FTPFile[] files = engine.getNext(25);

					for (FTPFile ftpFile : files) {
						builder.append(new Uri("ftp", null, uri.getHost(), uri.getPort(), uri.getPath() + "/" + ftpFile.getName(), null).toString() + "\n");
					}
				}

				String payload = builder.toString();
				Blob blob = new Blob(new BlobMetadata().setUri(uri).setSize(Long.valueOf(payload.length())).setFetchMetadata(metadata)).payload(payload);
				return blob;
			}
		} catch (IOException e) {
			ftpClientPool.returnBrokenResource(hostAndPort, ftpClient);
		} finally {
			ftpClientPool.returnResource(hostAndPort, ftpClient);
		}
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
