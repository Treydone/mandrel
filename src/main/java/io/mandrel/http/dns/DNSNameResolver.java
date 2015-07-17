package io.mandrel.http.dns;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsServerAddresses;
import io.netty.util.internal.ThreadLocalRandom;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import lombok.Data;

@Data
public class DNSNameResolver implements NameResolver {

	private static final long serialVersionUID = -623963740271067479L;

	private static final List<InetSocketAddress> SERVERS = Arrays.asList(new InetSocketAddress("8.8.8.8", 53), // Google
																												// Public
																												// DNS
			new InetSocketAddress("8.8.4.4", 53), new InetSocketAddress("208.67.222.222", 53), // OpenDNS
			new InetSocketAddress("208.67.220.220", 53), new InetSocketAddress("37.235.1.174", 53), // FreeDNS
			new InetSocketAddress("37.235.1.177", 53));

	private final EventLoopGroup group;
	private final DnsNameResolver resolver;

	public DNSNameResolver() {
		group = new NioEventLoopGroup(1);
		resolver = new DnsNameResolver(group.next(), NioDatagramChannel.class, DnsServerAddresses.shuffled(SERVERS));
		resolver.setMaxTriesPerQuery(SERVERS.size());
		resolver.setTtl(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public InetAddress resolve(String name) throws UnknownHostException {
		InetSocketAddress unresolved = InetSocketAddress.createUnresolved(name, ThreadLocalRandom.current().nextInt(65536));
		try {
			return resolver.resolve(unresolved).get().getAddress();
		} catch (InterruptedException | ExecutionException e) {
			UnknownHostException unknownHostException = new UnknownHostException(name);
			unknownHostException.setStackTrace(e.getStackTrace());
			throw unknownHostException;
		}
	}
}
