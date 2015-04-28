package io.mandrel.data.content.selector;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component
public class SelectorService {

	private Map<String, Selector<?>> selectorsByName;

	public SelectorService() {
		ClassLoader ctxtLoader = Thread.currentThread().getContextClassLoader();
		init(ctxtLoader);
	}

	private void init(final ClassLoader loader) {
		selectorsByName = new HashMap<>();
		initEngines(loader);
	}

	private ServiceLoader<Selector> getServiceLoader(final ClassLoader loader) {
		if (loader != null) {
			return ServiceLoader.load(Selector.class, loader);
		} else {
			return ServiceLoader.loadInstalled(Selector.class);
		}
	}

	private void initEngines(final ClassLoader loader) {
		Iterator<Selector> itr = null;
		try {
			ServiceLoader<Selector> sl = AccessController.doPrivileged(new PrivilegedAction<ServiceLoader<Selector>>() {
				@Override
				public ServiceLoader<Selector> run() {
					return getServiceLoader(loader);
				}
			});

			itr = sl.iterator();
		} catch (ServiceConfigurationError err) {
			log.debug("Can't find Selector providers: " + err.getMessage());
			// do not throw any exception here. user may want to
			// manage his/her own factories using this manager
			// by explicit registratation (by registerXXX) methods.
			return;
		}

		try {
			while (itr.hasNext()) {
				try {
					Selector<?> fact = itr.next();
					selectorsByName.put(fact.getName(), fact);
					log.debug("Selectors {} ({}) added", fact.getName(), fact.getClass());
				} catch (ServiceConfigurationError err) {
					log.debug("Selectors providers.next(): " + err.getMessage());
					// one factory failed, but check other factories...
					continue;
				}
			}
		} catch (ServiceConfigurationError err) {
			log.debug("Selectors providers.hasNext(): " + err.getMessage());
			// do not throw any exception here. user may want to
			// manage his/her own factories using this manager
			// by explicit registratation (by registerXXX) methods.
			return;
		}
	}

	public Selector<?> getSelectorByName(String shortName) {
		if (shortName == null)
			throw new NullPointerException();
		Selector<?> obj;
		if (null != (obj = selectorsByName.get(shortName))) {
			return obj;
		}
		return null;
	}

}
