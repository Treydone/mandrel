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
package io.mandrel.worker;

import io.mandrel.common.data.Spider;
import io.mandrel.common.thrift.TClient;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.netflix.servo.util.Throwables;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WorkerClient {

	private TClient client;

	public void create(Spider spider) {
		try {
			client.workerClient().create(null);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}

	public void start(Long spiderId) {
		try {
			client.workerClient().start(spiderId);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}

	public void pause(Long spiderId) {
		try {
			client.workerClient().pause(spiderId);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}

	public void kill(Long spiderId) {
		try {
			client.workerClient().kill(spiderId);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}
}
