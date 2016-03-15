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

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class NamedProviders {

	private final static LoadingCache<Class<? extends NamedDefinition>, Map<String, ? extends NamedDefinition>> cache = CacheBuilder.newBuilder().build(
			new CacheLoader<Class<? extends NamedDefinition>, Map<String, ? extends NamedDefinition>>() {
				@Override
				public Map<String, ? extends NamedDefinition> load(Class<? extends NamedDefinition> clazz) throws Exception {
					return NamedProviderLoader.create(clazz).getProvidersByName();
				}
			});

	public static <T extends NamedDefinition> Map<String, T> get(Class<T> clazz) {
		try {
			return (Map<String, T>) cache.get(clazz);
		} catch (ExecutionException e) {
			throw Throwables.propagate(e);
		}
	}

	public static <T extends NamedDefinition> T get(Class<T> clazz, String name) {
		NamedDefinition namedComponent = null;
		try {
			namedComponent = cache.get(clazz).get(name);
		} catch (ExecutionException e) {
			throw Throwables.propagate(e);
		}
		return (T) namedComponent;
	}
}
