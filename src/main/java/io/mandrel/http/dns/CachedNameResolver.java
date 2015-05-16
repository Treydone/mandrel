package io.mandrel.http.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Throwables;

@Data
@Slf4j
public class CachedNameResolver implements NameResolver {

	private static final long serialVersionUID = 4179034020754804054L;

	private ConcurrentMap<String, InetAddress> addresses = new ConcurrentHashMap<>();

	public InetAddress resolve(String name) throws UnknownHostException {
		return name != null ? addresses.computeIfAbsent(name, key -> {
			try {
				return InetAddress.getByName(key);
			} catch (UnknownHostException e) {
				log.debug("", e);
				throw Throwables.propagate(e);
			}
		}) : null;
	}
}
