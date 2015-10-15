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

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import com.facebook.nifty.test.LogEntry;
import com.facebook.nifty.test.Scribe;
import com.google.common.net.HostAndPort;
import com.netflix.servo.util.Throwables;

@Slf4j
public class ThriftClientTest {

	public static void main(String[] args) throws Exception {

		GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
		poolConfig.setMaxTotal(80);
		poolConfig.setMinIdlePerKey(4);
		poolConfig.setMaxWaitMillis(3000);
		poolConfig.setBlockWhenExhausted(true);

		try (TKeyedClientPool<Scribe.Client> pool = new TKeyedClientPool<Scribe.Client>(tProtocol -> new Scribe.Client(tProtocol), poolConfig)) {
			HostAndPort hostAndPort = HostAndPort.fromParts("localhost", 7911);

			ExecutorService executor = Executors.newFixedThreadPool(10);
			for (int i = 0; i < 10; i++) {
				final int value = i;
				executor.submit(new Runnable() {
					public void run() {

						for (int k = 0; k < 100; k++) {
							Scribe.Client resource = null;
							try {
								resource = pool.getResource(hostAndPort);
							} catch (Exception e) {
								log.warn("Can not get resource from the pool", e);
								throw Throwables.propagate(e);
							}

							try {
								for (int j = 0; j < 1000; j++) {
									try {
										resource.log(Collections.singletonList(new LogEntry("cat" + value, "test-" + k + "-" + j)));
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								pool.returnResource(hostAndPort, resource);
							} catch (Exception e) {
								try {
									pool.returnBrokenResource(hostAndPort, resource);
								} catch (Exception e1) {
									log.warn("whut???", e1);
								}
								log.warn("whut?", e);
							}

						}
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							log.warn("whhuuuuut?", e);
						}
					}

				});
			}
			executor.awaitTermination(60000, TimeUnit.MILLISECONDS);
		}
	}
}
