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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TZlibTransport;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.facebook.nifty.test.LogEntry;
import com.facebook.nifty.test.ResultCode;
import com.facebook.nifty.test.Scribe;

@Slf4j
public class ThriftServerTest {

	public static void main(String[] args) throws TTransportException {

		final MetricRegistry registry = new MetricRegistry();
		final Meter requests = registry.meter(MetricRegistry.name(ThriftServerTest.class, "requests"));
		final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
		reporter.start(1, TimeUnit.SECONDS);

		// Create the handler
		Scribe.Iface serviceInterface = new Scribe.Iface() {

			private AtomicInteger value = new AtomicInteger();

			@Override
			public ResultCode log(List<LogEntry> messages) throws TException {
				requests.mark();
				for (LogEntry message : messages) {
//					if (value.get() % 1000 == 0) {
//						log.info("{}: {}", message.getCategory(), message.getMessage());
//					}
					value.incrementAndGet();
				}
				return ResultCode.OK;
			}
		};

		TServerSocket serverTransport = new TServerSocket(7911);
		Scribe.Processor<Scribe.Iface> processor = new Scribe.Processor<Scribe.Iface>(serviceInterface);

		TZlibTransport.Factory factory = new TZlibTransport.Factory();
		final TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor).outputTransportFactory(factory)
				.inputTransportFactory(factory));

		server.serve();

		// Arrange to stop the server at shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				server.stop();
			}
		});
	}
}
