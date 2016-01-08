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
