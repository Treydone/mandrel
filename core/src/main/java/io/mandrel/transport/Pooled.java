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
package io.mandrel.transport;

import io.mandrel.transport.thrift.TClientException;

import java.util.function.Consumer;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import com.facebook.swift.service.RuntimeTException;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

@RequiredArgsConstructor(staticName = "of")
@Slf4j
public class Pooled<T> {

	private final GenericKeyedObjectPool<HostAndPort, T> internalPool;
	private final HostAndPort hostAndPort;

	public <R> R map(Function<? super T, ? extends R> mapper) {
		T pooled = null;
		try {
			pooled = internalPool.borrowObject(hostAndPort);
			return mapper.apply(pooled);
		} catch (RuntimeTException e) {
			try {
				internalPool.invalidateObject(hostAndPort, pooled);
				pooled = null;
			} catch (Exception e1) {
				log.warn("", e1);
			}
			throw e;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		} finally {
			if (pooled != null) {
				try {
					internalPool.returnObject(hostAndPort, pooled);
				} catch (Exception e) {
					throw new TClientException("Could not return the resource to the pool", e);
				}
			}
		}
	}

	public void with(Consumer<? super T> action) {
		T pooled = null;
		try {
			pooled = internalPool.borrowObject(hostAndPort);
			action.accept(pooled);
		} catch (RuntimeTException e) {
			try {
				internalPool.invalidateObject(hostAndPort, pooled);
				pooled = null;
			} catch (Exception e1) {
				log.warn("", e1);
			}
			throw e;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		} finally {
			if (pooled != null) {
				try {
					internalPool.returnObject(hostAndPort, pooled);
				} catch (Exception e) {
					throw new TClientException("Could not return the resource to the pool", e);
				}
			}
		}
	}
}
