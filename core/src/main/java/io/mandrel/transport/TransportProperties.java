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

import io.mandrel.common.unit.TimeValue;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "transport")
public class TransportProperties {

	@Min(0)
	@Max(65535)
	private int port = 9090;
	private String bindAddress = "localhost";

	private TimeValue connectTimeout = new TimeValue(500, TimeUnit.MILLISECONDS);
	private TimeValue receiveTimeout = new TimeValue(1, TimeUnit.MINUTES);
	private TimeValue readTimeout = new TimeValue(30, TimeUnit.SECONDS);
	private TimeValue writeTimeout = new TimeValue(1, TimeUnit.MINUTES);
	// Default max frame size of 16 MB
	private int maxFrameSize = 16777216;

}
