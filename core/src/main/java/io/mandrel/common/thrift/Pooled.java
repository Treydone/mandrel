package io.mandrel.common.thrift;

import java.util.function.Consumer;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.weakref.jmx.internal.guava.base.Throwables;

import com.facebook.swift.service.RuntimeTException;
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
