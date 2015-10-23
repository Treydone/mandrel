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
///*
// * Licensed to Mandrel under one or more contributor
// * license agreements. See the NOTICE file distributed with
// * this work for additional information regarding copyright
// * ownership. Mandrel licenses this file to you under
// * the Apache License, Version 2.0 (the "License"); you may
// * not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package io.mandrel.common.thrift;
//
//import java.util.zip.Deflater;
//
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//
//import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
//import org.apache.commons.pool2.PooledObject;
//import org.apache.commons.pool2.impl.DefaultPooledObject;
//import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
//import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
//import org.apache.thrift.TServiceClient;
//import org.apache.thrift.protocol.TBinaryProtocol;
//import org.apache.thrift.protocol.TProtocol;
//import org.apache.thrift.transport.TSocket;
//import org.apache.thrift.transport.TTransport;
//import org.apache.thrift.transport.TTransportException;
//import org.apache.thrift.transport.TZlibTransport;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.google.common.net.HostAndPort;
//
//@Data
//public class TMultiplexedClientPool implements AutoCloseable {
//
//	private static final Logger LOGGER = LoggerFactory.getLogger(TMultiplexedClientPool.class);
//
//	private static int DEFAULT_PORT = 80;
//
//	private static Integer DEFAULT_DEFLATE_LEVEL = Integer.valueOf(Deflater.BEST_COMPRESSION);
//
//	private final GenericKeyedObjectPool<HostAndPort, TTransport> internalPool;
//
//	private final ProtocolFactory protocolFactory;
//
//	public TMultiplexedClientPool(ClientFactory<T> clientFactory, GenericKeyedObjectPoolConfig poolConfig) {
//		this(clientFactory, poolConfig, DEFAULT_PORT, DEFAULT_DEFLATE_LEVEL);
//	}
//
//	public TMultiplexedClientPool(ClientFactory<T> clientFactory, GenericKeyedObjectPoolConfig poolConfig, int defaultPort, Integer deflateLevel) {
//		this(clientFactory, new BinaryOverSocketProtocolFactory(deflateLevel, deflateLevel), poolConfig);
//	}
//
//	public TMultiplexedClientPool(ClientFactory<T> clientFactory, ProtocolFactory protocolFactory, GenericKeyedObjectPoolConfig poolConfig) {
//		this.protocolFactory = protocolFactory;
//		this.internalPool = new GenericKeyedObjectPool<HostAndPort, TTransport>(new ThriftClientFactory(clientFactory, protocolFactory), poolConfig);
//	}
//
//	@FunctionalInterface
//	public interface ClientFactory<T> {
//
//		T make(TProtocol tProtocol);
//	}
//
//	@FunctionalInterface
//	public interface ProtocolFactory {
//
//		TProtocol make(HostAndPort hostAndPort);
//	}
//
//	class ThriftClientFactory extends BaseKeyedPooledObjectFactory<HostAndPort, TTransport> {
//
//		private ClientFactory<T> clientFactory;
//		private ProtocolFactory protocolFactory;
//
//		public ThriftClientFactory(ClientFactory<T> clientFactory, ProtocolFactory protocolFactory) {
//			this.clientFactory = clientFactory;
//			this.protocolFactory = protocolFactory;
//		}
//
//		@Override
//		public void destroyObject(HostAndPort hostAndPort, PooledObject<T> obj) throws Exception {
//			if (obj.getObject().getOutputProtocol().getTransport().isOpen()) {
//				obj.getObject().getOutputProtocol().getTransport().close();
//			}
//			if (obj.getObject().getInputProtocol().getTransport().isOpen()) {
//				obj.getObject().getInputProtocol().getTransport().close();
//			}
//		}
//
//		@Override
//		public PooledObject<T> makeObject(HostAndPort hostAndPort) throws Exception {
//			return wrap(create(hostAndPort));
//		}
//
//		@Override
//		public T create(HostAndPort hostAndPort) throws Exception {
//			try {
//				TProtocol protocol = protocolFactory.make(hostAndPort);
//				return clientFactory.make(protocol);
//			} catch (Exception e) {
//				LOGGER.warn("whut?", e);
//				throw new TClientException("Can not make a new object for pool", e);
//			}
//		}
//
//		@Override
//		public PooledObject<T> wrap(T value) {
//			return new DefaultPooledObject<T>(value);
//		}
//	}
//
//	@Data
//	@RequiredArgsConstructor
//	public static class BinaryOverSocketProtocolFactory implements ProtocolFactory {
//
//		private final int defaultPort;
//
//		private final Integer deflateLevel;
//
//		public TProtocol make(HostAndPort hostAndPort) {
//
//			TTransport transport = new TSocket(hostAndPort.getHostText(), hostAndPort.getPortOrDefault(defaultPort));
//			if (deflateLevel != null) {
//				transport = new TZlibTransport(transport, 0);
//			}
//			try {
//				transport.open();
//			} catch (TTransportException e) {
//				LOGGER.warn("whut?", e);
//				transport.close();
//				throw new TClientException("Can not make protocol", e);
//			}
//			return new TBinaryProtocol(transport);
//		}
//	}
//
//	public T getResource(HostAndPort hostAndPort) {
//		try {
//			return internalPool.borrowObject(hostAndPort);
//		} catch (Exception e) {
//			throw new TClientException("Could not get a resource from the pool", e);
//		}
//	}
//
//	public void returnResourceObject(HostAndPort hostAndPort, T resource) {
//		try {
//			internalPool.returnObject(hostAndPort, resource);
//		} catch (Exception e) {
//			throw new TClientException("Could not return the resource to the pool", e);
//		}
//	}
//
//	public void returnBrokenResource(HostAndPort hostAndPort, T resource) {
//		returnBrokenResourceObject(hostAndPort, resource);
//	}
//
//	public void returnResource(HostAndPort hostAndPort, T resource) {
//		returnResourceObject(hostAndPort, resource);
//	}
//
//	protected void returnBrokenResourceObject(HostAndPort hostAndPort, T resource) {
//		try {
//			internalPool.invalidateObject(hostAndPort, resource);
//		} catch (Exception e) {
//			throw new TClientException("Could not return the resource to the pool", e);
//		}
//	}
//
//	public void destroy() {
//		close();
//	}
//
//	public void close() {
//		try {
//			internalPool.close();
//		} catch (Exception e) {
//			throw new TClientException("Could not destroy the pool", e);
//		}
//	}
//}