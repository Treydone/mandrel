package io.mandrel.common.loader;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class NamedProviders {

	private final static LoadingCache<Class<? extends NamedComponent>, Map<String, ? extends NamedComponent>> cache = CacheBuilder.newBuilder().build(
			new CacheLoader<Class<? extends NamedComponent>, Map<String, ? extends NamedComponent>>() {
				@Override
				public Map<String, ? extends NamedComponent> load(Class<? extends NamedComponent> clazz) throws Exception {
					return NamedProviderLoader.create(clazz).getProvidersByName();
				}
			});

	public static <T extends NamedComponent> Map<String, T> get(Class<T> clazz) {
		try {
			return (Map<String, T>) cache.get(clazz);
		} catch (ExecutionException e) {
			throw Throwables.propagate(e);
		}
	}

	public static <T extends NamedComponent> T get(Class<T> clazz, String name) {
		NamedComponent namedComponent = null;
		try {
			namedComponent = cache.get(clazz).get(name);
		} catch (ExecutionException e) {
			throw Throwables.propagate(e);
		}
		return (T) namedComponent;
	}
}
