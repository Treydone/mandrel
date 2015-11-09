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
package io.mandrel.common.client;

import io.mandrel.endpoints.contracts.AdminContract;
import io.mandrel.endpoints.contracts.FrontierContract;
import io.mandrel.endpoints.contracts.NodeContract;
import io.mandrel.endpoints.contracts.WorkerContract;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.netflix.feign.support.ResponseEntityDecoder;
import org.springframework.cloud.netflix.feign.support.SpringDecoder;
import org.springframework.cloud.netflix.feign.support.SpringEncoder;
import org.springframework.stereotype.Component;

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
import feign.slf4j.Slf4jLogger;

@Component
public class Clients {

	@Autowired
	private ObjectFactory<HttpMessageConverters> messageConverters;

	public Decoder feignDecoder() {
		return new ResponseEntityDecoder(new SpringDecoder(messageConverters));
	}

	public Encoder feignEncoder() {
		return new SpringEncoder(messageConverters);
	}

	public Logger feignLogger() {
		return new Slf4jLogger();
	}

	public Contract feignContract() {
		return new SpringMvcContract();
	}

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
				.logger(this.feignLogger()).encoder(this.feignEncoder()).decoder(this.feignDecoder()).contract(this.feignContract()).client(this.client);

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

	private WorkerContract worker;
	private FrontierContract frontier;
	private AdminContract admin;
	private NodeContract common;

	@PostConstruct
	public void init() {
		worker = feign(WorkerContract.class);
		frontier = feign(FrontierContract.class);
		admin = feign(AdminContract.class);
		common = feign(NodeContract.class);
	}

	public WorkerContract workerClient() {
		return worker;
	}

	public FrontierContract frontierClient() {
		return frontier;
	}

	public AdminContract controllerClient() {
		return admin;
	}

	public NodeContract nodeClient() {
		return common;
	}
}
