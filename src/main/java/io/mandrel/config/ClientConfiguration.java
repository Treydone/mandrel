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
package io.mandrel.config;

import io.mandrel.controller.AdminClient;
import io.mandrel.frontier.FrontierClient;
import io.mandrel.worker.WorkerClient;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Client;
import feign.Client.Default;
import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.Target.EmptyTarget;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;

@Configuration
public class ClientConfiguration {

	@Autowired
	private Decoder decoder;

	@Autowired
	private Encoder encoder;

	@Autowired
	private Logger logger;

	@Autowired
	private Contract contract;

	@Autowired(required = false)
	private Logger.Level logLevel;

	@Autowired(required = false)
	private Retryer retryer;

	@Autowired(required = false)
	private ErrorDecoder errorDecoder;

	@Autowired(required = false)
	private Request.Options options;

	private Client client = new Default(null, null);

	@Autowired(required = false)
	private List<RequestInterceptor> requestInterceptors;

	protected <T> T feign(Class<T> clazz) {
		Feign.Builder builder = Feign.builder()
		// required values
				.logger(this.logger).encoder(this.encoder).decoder(this.decoder).contract(this.contract).client(this.client);

		// optional values
		if (this.logLevel != null) {
			builder.logLevel(this.logLevel);
		}
		if (this.retryer != null) {
			builder.retryer(this.retryer);
		}
		if (this.errorDecoder != null) {
			builder.errorDecoder(this.errorDecoder);
		}
		if (this.options != null) {
			builder.options(this.options);
		}
		if (this.requestInterceptors != null) {
			builder.requestInterceptors(this.requestInterceptors);
		}

		return builder.target(EmptyTarget.create(clazz));
	}

	@Bean
	public WorkerClient workerClient() {
		return feign(WorkerClient.class);
	}

	@Bean
	public FrontierClient frontierClient() {
		return feign(FrontierClient.class);
	}

	@Bean
	public AdminClient controllerClient() {
		return feign(AdminClient.class);
	}
}
