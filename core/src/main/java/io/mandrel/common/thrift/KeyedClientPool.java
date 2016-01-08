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

import io.mandrel.endpoints.contracts.Contract;

import java.util.zip.Deflater;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TZlibTransport;

import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.nifty.client.NiftyClientChannel;
import com.facebook.nifty.client.NiftyClientConnector;
import com.facebook.nifty.duplex.TDuplexProtocolFactory;
import com.facebook.nifty.duplex.TProtocolPair;
import com.facebook.nifty.duplex.TTransportPair;
import com.facebook.swift.service.ThriftClientManager;
import com.google.common.net.HostAndPort;

@Data
public class KeyedClientPool<T extends Contract & AutoCloseable> implements AutoCloseable {

	private static int DEFAULT_PORT = 9090;
	private static Integer DEFAULT_DEFLATE_LEVEL = Integer.valueOf(Deflater.BEST_SPEED);

	private final GenericKeyedObjectPool<HostAndPort, T> internalPool;
	private final ThriftClientManager clientManager;

	public KeyedClientPool(Class<T> clazz) {
		this(clazz, new GenericKeyedObjectPoolConfig(), DEFAULT_PORT, DEFAULT_DEFLATE_LEVEL, new ThriftClientManager());
	}

	public KeyedClientPool(Class<T> clazz, GenericKeyedObjectPoolConfig poolConfig) {
		this(clazz, poolConfig, DEFAULT_PORT, DEFAULT_DEFLATE_LEVEL, new ThriftClientManager());
	}

	public KeyedClientPool(Class<T> clazz, GenericKeyedObjectPoolConfig poolConfig, ThriftClientManager clientManager) {
		this(clazz, poolConfig, DEFAULT_PORT, DEFAULT_DEFLATE_LEVEL, clientManager);
	}

	public KeyedClientPool(Class<T> clazz, GenericKeyedObjectPoolConfig poolConfig, int defaultPort, Integer deflateLevel, ThriftClientManager clientManager) {
		this(clazz, new FramedClientConnectorFactory(defaultPort, deflateLevel), poolConfig, clientManager);
	}

	public KeyedClientPool(Class<T> clazz, ClientConnectorFactory clientConnectorFactory, GenericKeyedObjectPoolConfig poolConfig,
			ThriftClientManager clientManager) {
		this.clientManager = clientManager;
		this.internalPool = new GenericKeyedObjectPool<HostAndPort, T>(new ClientObjectFactory(clientConnectorFactory, clientManager, clazz), poolConfig);
	}

	@FunctionalInterface
	public interface ClientFactory<T> {
		T make(NiftyClientConnector<? extends NiftyClientChannel> connector);
	}

	@FunctionalInterface
	public interface ClientConnectorFactory {
		NiftyClientConnector<? extends NiftyClientChannel> make(HostAndPort hostAndPort);
	}

	@RequiredArgsConstructor
	class ClientObjectFactory extends BaseKeyedPooledObjectFactory<HostAndPort, T> {

		private final ClientConnectorFactory clientConnectorFactory;
		private final ThriftClientManager clientManager;
		private final Class<T> clazz;

		@Override
		public void destroyObject(HostAndPort hostAndPort, PooledObject<T> obj) throws Exception {
			obj.getObject().close();
		}

		@Override
		public PooledObject<T> makeObject(HostAndPort hostAndPort) throws Exception {
			return wrap(create(hostAndPort));
		}

		@Override
		public T create(HostAndPort hostAndPort) throws Exception {
			NiftyClientConnector<? extends NiftyClientChannel> connector = clientConnectorFactory.make(hostAndPort);
			return clientManager.createClient(connector, clazz).get();
		}

		@Override
		public PooledObject<T> wrap(T value) {
			return new DefaultPooledObject<T>(value);
		}
	}

	public static final TDuplexProtocolFactory protocolFactory(Integer deflat) {
		return new TDuplexProtocolFactory() {
			private final TProtocolFactory protocolFactory =
			// new TBinaryProtocol.Factory();
			new TCompactProtocol.Factory(1024);

			@Override
			public TProtocolPair getProtocolPair(TTransportPair transportPair) {
				TTransport inputTransport = transportPair.getInputTransport();
				TTransport outputTransport = transportPair.getOutputTransport();

				if (deflat != null) {
					inputTransport = new TZlibTransport(transportPair.getInputTransport(), deflat);
					outputTransport = new TZlibTransport(transportPair.getOutputTransport(), deflat);
				}
				return TProtocolPair.fromSeparateProtocols(protocolFactory.getProtocol(inputTransport), protocolFactory.getProtocol(outputTransport));
			}
		};
	}

	@Data
	@RequiredArgsConstructor
	public static class FramedClientConnectorFactory implements ClientConnectorFactory {

		private final int defaultPort;
		private final Integer deflateLevel;

		public NiftyClientConnector<? extends NiftyClientChannel> make(HostAndPort hostAndPort) {
			TDuplexProtocolFactory duplexProtocolFactory = protocolFactory(deflateLevel);
			return new FramedClientConnector(HostAndPort.fromParts(hostAndPort.getHostText(), hostAndPort.getPortOrDefault(defaultPort)), duplexProtocolFactory);
		}
	}

	public Pooled<T> get(HostAndPort hostAndPort) {
		return Pooled.of(internalPool, hostAndPort);
	}

	public T getResource(HostAndPort hostAndPort) {
		try {
			return internalPool.borrowObject(hostAndPort);
		} catch (Exception e) {
			throw new TClientException("Could not get a resource from the pool", e);
		}
	}

	public void returnBrokenResource(HostAndPort hostAndPort, T resource) {
		returnBrokenResourceObject(hostAndPort, resource);
	}

	public void returnResource(HostAndPort hostAndPort, T resource) {
		returnResourceObject(hostAndPort, resource);
	}

	protected void returnBrokenResourceObject(HostAndPort hostAndPort, T resource) {
		try {
			internalPool.invalidateObject(hostAndPort, resource);
		} catch (Exception e) {
			throw new TClientException("Could not return the resource to the pool", e);
		}
	}

	protected void returnResourceObject(HostAndPort hostAndPort, T resource) {
		try {
			internalPool.returnObject(hostAndPort, resource);
		} catch (Exception e) {
			throw new TClientException("Could not return the resource to the pool", e);
		}
	}

	public void destroy() {
		close();
	}

	public void close() {
		try {
			internalPool.close();
		} catch (Exception e) {
			throw new TClientException("Could not destroy the pool", e);
		}
		try {
			clientManager.close();
		} catch (Exception e) {
			throw new TClientException("Could not destroy the pool", e);
		}
	}
}