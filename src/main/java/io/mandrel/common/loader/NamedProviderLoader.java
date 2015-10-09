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
					providersByName.put(fact.getName(), fact);
					log.warn("Provider for {} {} ({}) added", clazz.getName(), fact.getName(), fact.getClass());
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
