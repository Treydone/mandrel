package io.mandrel.common.thrift;

import io.mandrel.common.thrift.ThriftClientPool.ClientFactory;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.apache.thrift.protocol.TProtocol;

import com.facebook.nifty.test.LogEntry;
import com.facebook.nifty.test.Scribe;
import com.facebook.nifty.test.Scribe.Client;

@Slf4j
public class ThriftClientTest {

	public static void main(String[] args) throws Exception {

		Config poolConfig = new Config();
		poolConfig.maxActive = 80;
		poolConfig.minIdle = 5;
		poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
		poolConfig.testOnBorrow = true;
		poolConfig.testWhileIdle = true;
		poolConfig.numTestsPerEvictionRun = 10;
		poolConfig.maxWait = 3000;

		final ThriftClientPool<Scribe.Client> pool = new ThriftClientPool<Scribe.Client>(new ClientFactory<Scribe.Client>() {
			@Override
			public Client make(TProtocol tProtocol) {
				return new Scribe.Client(tProtocol);
			}
		}, poolConfig, "localhost", 7911);

		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 10; i++) {
			executor.submit(new Runnable() {
				public void run() {

					Scribe.Client resource = pool.getResource();
					try {
						for (int i = 0; i < 10000; i++) {
							try {
								resource.log(Collections.singletonList(new LogEntry("cat1", "test" + i)));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						pool.returnResource(resource);
					} catch (Exception e) {
						pool.returnBrokenResource(resource);
						log.warn("whut?", e);
					}
				}
			});
		}

		Thread.sleep(3000);
		pool.close();
	}
}
