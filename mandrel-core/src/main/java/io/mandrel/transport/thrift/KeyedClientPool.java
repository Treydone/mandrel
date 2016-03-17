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

import io.airlift.units.Duration;
import io.mandrel.endpoints.contracts.Contract;
import io.mandrel.transport.Pooled;
import io.mandrel.transport.thrift.nifty.ThriftClientManager;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TZlibTransport;
import org.jboss.netty.channel.local.LocalAddress;

import com.facebook.nifty.client.GenericFramedClientConnector;
import com.facebook.nifty.client.NiftyClientChannel;
import com.facebook.nifty.client.NiftyClientConnector;
import com.facebook.nifty.duplex.TDuplexProtocolFactory;
import com.facebook.nifty.duplex.TProtocolPair;
import com.facebook.nifty.duplex.TTransportPair;
import com.facebook.swift.service.ThriftClientEventHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;

@Data
public class KeyedClientPool<T extends Contract & AutoCloseable> implements AutoCloseable {

	private static int DEFAULT_PORT = 9090;
	private static Integer DEFAULT_DEFLATE_LEVEL = Integer.valueOf(Deflater.BEST_SPEED);

	public static final Duration DEFAULT_CONNECT_TIMEOUT = new Duration(500, TimeUnit.MILLISECONDS);
	public static final Duration DEFAULT_RECEIVE_TIMEOUT = new Duration(1, TimeUnit.MINUTES);
	public static final Duration DEFAULT_READ_TIMEOUT = new Duration(30, TimeUnit.SECONDS);
	public static final Duration DEFAULT_WRITE_TIMEOUT = new Duration(10, TimeUnit.MINUTES);
	// Default max frame size of 16 MB
	public static final int DEFAULT_MAX_FRAME_SIZE = 16777216;

	private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	private Duration receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;
	private Duration readTimeout = DEFAULT_READ_TIMEOUT;
	private Duration writeTimeout = DEFAULT_WRITE_TIMEOUT;
	private int maxFrameSize = DEFAULT_MAX_FRAME_SIZE;

	private final GenericKeyedObjectPool<HostAndPort, T> internalPool;
	private final ThriftClientManager clientManager;

	public KeyedClientPool(Class<T> clazz) {
		this(clazz, new GenericKeyedObjectPoolConfig(), DEFAULT_PORT, DEFAULT_DEFLATE_LEVEL, new ThriftClientManager(), false);
	}

	public KeyedClientPool(Class<T> clazz, GenericKeyedObjectPoolConfig poolConfig) {
		this(clazz, poolConfig, DEFAULT_PORT, DEFAULT_DEFLATE_LEVEL, new ThriftClientManager(), false);
	}

	public KeyedClientPool(Class<T> clazz, GenericKeyedObjectPoolConfig poolConfig, ThriftClientManager clientManager) {
		this(clazz, poolConfig, DEFAULT_PORT, DEFAULT_DEFLATE_LEVEL, clientManager, false);
	}

	public KeyedClientPool(Class<T> clazz, GenericKeyedObjectPoolConfig poolConfig, int defaultPort, Integer deflateLevel, ThriftClientManager clientManager,
			boolean local) {
		this(clazz, new FramedClientConnectorFactory(defaultPort, deflateLevel, local), poolConfig, clientManager);
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
			return clientManager.createClient(connector, clazz, connectTimeout, receiveTimeout, readTimeout, writeTimeout, maxFrameSize, clazz.getSimpleName(),
					ImmutableList.<ThriftClientEventHandler> of(), clientManager.getDefaultSocksProxy()).get(10000, TimeUnit.MILLISECONDS);
		}

		@Override
		public PooledObject<T> wrap(T value) {
			return new DefaultPooledObject<T>(value);
		}
	}

	public static final TDuplexProtocolFactory protocolFactory(Integer deflat) {
		return new TDuplexProtocolFactory() {
			private final TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();

			// new TCompactProtocol.Factory(1024);

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
		private final boolean local;

		public NiftyClientConnector<? extends NiftyClientChannel> make(HostAndPort hostAndPort) {
			TDuplexProtocolFactory duplexProtocolFactory = protocolFactory(deflateLevel);
			SocketAddress address = local ? new LocalAddress("mandrel") : new InetSocketAddress(hostAndPort.getHostText(),
					hostAndPort.getPortOrDefault(defaultPort));
			return new GenericFramedClientConnector(address, duplexProtocolFactory);
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