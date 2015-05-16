package io.mandrel.http.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.Data;

@Data
public class InternalNameResolver implements NameResolver {

	private static final long serialVersionUID = -7534644889369417852L;

	public InetAddress resolve(String name) throws UnknownHostException {
		return InetAddress.getByName(name);
	}
}
