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
package io.mandrel.common.loader;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NamedProviderLoader<T extends NamedComponent> {

	private Map<String, T> providersByName;

	public static <U extends NamedComponent> NamedProviderLoader<U> create(Class<U> clazz) {
		NamedProviderLoader<U> dynamicLoader = new NamedProviderLoader<U>();
		ClassLoader ctxtLoader = Thread.currentThread().getContextClassLoader();
		dynamicLoader.init(ctxtLoader, clazz);
		return dynamicLoader;
	}

	private ServiceLoader<T> getServiceLoader(final ClassLoader loader, final Class<T> clazz) {
		if (loader != null) {
			return ServiceLoader.load(clazz, loader);
		} else {
			return ServiceLoader.loadInstalled(clazz);
		}
	}

	private void init(final ClassLoader loader, final Class<T> clazz) {
		providersByName = new HashMap<>();
		Iterator<T> itr = null;
		try {
			ServiceLoader<T> sl = AccessController.doPrivileged(new PrivilegedAction<ServiceLoader<T>>() {
				@Override
				public ServiceLoader<T> run() {
					return getServiceLoader(loader, clazz);
				}
			});

			itr = sl.iterator();
		} catch (ServiceConfigurationError err) {
			log.warn("Can't find providers: " + err.getMessage());
			// do not throw any exception here. user may want to
			// manage his/her own factories using this manager
			// by explicit registratation (by registerXXX) methods.
			return;
		}

		try {
			while (itr.hasNext()) {
				try {
					T fact = itr.next();
					providersByName.put(fact.name(), fact);
					log.warn("Provider for {} {} ({}) added", clazz.getName(), fact.name(), fact.getClass());
				} catch (ServiceConfigurationError err) {
					log.warn("Provider for {} providers.next(): ", clazz.getName(), err);
					// one factory failed, but check other factories...
					continue;
				}
			}
		} catch (ServiceConfigurationError err) {
			log.warn("Provider for {} providers.hasNext(): ", clazz.getName(), err);
			// do not throw any exception here. user may want to
			// manage his/her own factories using this manager
			// by explicit registratation (by registerXXX) methods.
			return;
		}
	}

	public T getProviderByName(String shortName) {
		if (shortName == null)
			throw new NullPointerException();
		T obj;
		if (null != (obj = providersByName.get(shortName))) {
			return obj;
		}
		return null;
	}

	public Map<String, T> getProvidersByName() {
		return providersByName;
	}
}
