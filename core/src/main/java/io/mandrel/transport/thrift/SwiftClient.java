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
package io.mandrel.transport.thrift;
//package io.mandrel.common.thrift;
//
//import io.mandrel.endpoints.contractsv2.FrontierContract;
//
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import lombok.extern.slf4j.Slf4j;
//
//import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
//
//import com.facebook.swift.service.ThriftClientManager;
//import com.google.common.net.HostAndPort;
//
//@Slf4j
//public class SwiftClient {
//
//	public static void main(String[] args) throws InterruptedException, ExecutionException {
//		log.warn("Go!");
//		GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
//		poolConfig.setMaxTotalPerKey(20);
//		poolConfig.setMinIdlePerKey(1);
//		try (KeyedClientPool<FrontierContract> pool = new KeyedClientPool<>(FrontierContract.class, poolConfig, 9090,
//		// Deflater.BEST_SPEED
//				null, new ThriftClientManager())) {
//
//			ExecutorService executor = Executors.newFixedThreadPool(10);
//
//			for (int j = 0; j < 10; j++) {
//				executor.submit(() -> {
//					HostAndPort hostAndPort = HostAndPort.fromParts("localhost", 9090);
//					pool.get(hostAndPort).with(c -> {
//						for (int i = 0; i < 10000; i++) {
//							c.listContainers();
//						}
//					});
//				});
//			}
//			Thread.sleep(10000);
//		}
//	}
//}
