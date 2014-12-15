package io.mandrel.client.content.selector;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class SelectorService {

	private Map<String, WebPageSelector> selectorsByName;

	public SelectorService() {
		ClassLoader ctxtLoader = Thread.currentThread().getContextClassLoader();
		init(ctxtLoader);
	}

	public SelectorService(ClassLoader loader) {
		init(loader);
	}

	private void init(final ClassLoader loader) {
		selectorsByName = new HashMap<String, WebPageSelector>();
		initEngines(loader);
	}

	private ServiceLoader<WebPageSelector> getServiceLoader(
			final ClassLoader loader) {
		if (loader != null) {
			return ServiceLoader.load(WebPageSelector.class, loader);
		} else {
			return ServiceLoader.loadInstalled(WebPageSelector.class);
		}
	}

	private void initEngines(final ClassLoader loader) {
		Iterator<WebPageSelector> itr = null;
		try {
			ServiceLoader<WebPageSelector> sl = AccessController
					.doPrivileged(new PrivilegedAction<ServiceLoader<WebPageSelector>>() {
						@Override
						public ServiceLoader<WebPageSelector> run() {
							return getServiceLoader(loader);
						}
					});

			itr = sl.iterator();
		} catch (ServiceConfigurationError err) {
			System.err.println("Can't find WebPageSelector providers: "
					+ err.getMessage());
			// do not throw any exception here. user may want to
			// manage his/her own factories using this manager
			// by explicit registratation (by registerXXX) methods.
			return;
		}

		try {
			while (itr.hasNext()) {
				try {
					WebPageSelector fact = itr.next();
					selectorsByName.put(fact.getName(), fact);
				} catch (ServiceConfigurationError err) {
					System.err.println("Selectors providers.next(): "
							+ err.getMessage());
					// one factory failed, but check other factories...
					continue;
				}
			}
		} catch (ServiceConfigurationError err) {
			System.err.println("Selectors providers.hasNext(): "
					+ err.getMessage());
			// do not throw any exception here. user may want to
			// manage his/her own factories using this manager
			// by explicit registratation (by registerXXX) methods.
			return;
		}
	}

	public WebPageSelector getSelectorByName(String shortName) {
		if (shortName == null)
			throw new NullPointerException();
		WebPageSelector obj;
		if (null != (obj = selectorsByName.get(shortName))) {
			return obj;
		}
		return null;
	}
}
