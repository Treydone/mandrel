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
package io.mandrel.common.thrift;

import io.mandrel.controller.thrift.Controller;
import io.mandrel.frontier.thrift.Frontier;
import io.mandrel.worker.thrift.Worker;
import lombok.Getter;
import lombok.experimental.Accessors;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.netflix.servo.util.Throwables;

@Getter
@Accessors(fluent = true)
public class TClient {

	private Worker.Client workerClient;
	private Frontier.Client frontierClient;
	private Controller.Client controllerClient;

	public void init() {
		TTransport transport = new TFramedTransport(new TSocket("localhost", 9090));
		try {
			transport.open();
		} catch (TTransportException e) {
			transport.close();
			throw Throwables.propagate(e);
		}
		TProtocol protocol = new TBinaryProtocol(transport);

		TMultiplexedProtocol workerProtocol = new TMultiplexedProtocol(protocol, "Worker");
		workerClient = new Worker.Client(workerProtocol);

		TMultiplexedProtocol frontierProtocol = new TMultiplexedProtocol(protocol, "Frontier");
		frontierClient = new Frontier.Client(frontierProtocol);

		TMultiplexedProtocol controllerProtocol = new TMultiplexedProtocol(protocol, "Controller");
		controllerClient = new Controller.Client(controllerProtocol);

	}
}
