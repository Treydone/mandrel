package io.mandrel.cluster.discovery.atomix;

import io.atomix.Atomix;
import io.atomix.coordination.DistributedGroup;
import io.atomix.coordination.GroupMember;
import io.atomix.coordination.LocalGroupMember;
import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.DiscoveryProperties;
import io.mandrel.cluster.discovery.ServiceInstance;
import io.mandrel.cluster.discovery.ServiceInstance.ServiceInstanceBuilder;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@ConditionalOnProperty(value = "discovery.atomix.enabled", matchIfMissing = true)
@Component
@Slf4j
public class AtomixDiscoveryClient implements DiscoveryClient {

	@Autowired
	private Atomix atomix;
	@Autowired
	private DiscoveryProperties discoveryProperties;

	private final static String ID = UUID.randomUUID().toString();

	@Override
	public ServiceInstance register(ServiceInstance instance) {
		ServiceInstanceBuilder builder = ServiceInstance.builder().port(instance.getPort())
				.host(instance.getHost() != null ? instance.getHost() : getInstanceHost()).name(instance.getName());

		DistributedGroup group = atomix.getGroup(instance.getName()).join();
		group.join(ID).thenAccept(member -> {
			builder.id(member.id());
			member.set("port", instance.getPort());
			member.set("host", instance.getHost());
			log.info("Joined group {} with member ID {}", instance.getName(), member.id());
		});

		return builder.build();
	}

	@Override
	public void unregister(String serviceId) {
		// Weird...
		atomix.getGroup(serviceId).thenAccept(group -> group.join().thenAccept(member -> member.leave()));
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		return atomix.getGroup(serviceId).join().members().stream().map(member -> {
			return convert(serviceId, member);
		}).collect(Collectors.toList());
	}

	@Override
	public List<String> getServices() {
		throw new NotImplementedException();
	}

	@Override
	public String getInstanceHost() {
		String host = discoveryProperties.getInstanceHost() == null ? getIpAddress() : discoveryProperties.getInstanceHost();
		return host;
	}

	@Override
	public ServiceInstance getLocalInstance(String serviceId) {
		LocalGroupMember member = atomix.getGroup(serviceId).join().join().join();
		return convert(serviceId, member);
	}

	@Override
	public ServiceInstance getInstance(String id, String serviceId) {
		GroupMember member = atomix.getGroup(serviceId).join().member(id);
		return convert(serviceId, member);
	}

	@Override
	public String getInstanceId() {
		return ID;
	}

	protected ServiceInstance convert(String serviceId, GroupMember member) {
		Assert.notNull(member, "member can not be null");
		Integer port = (Integer) member.get("port").join();
		String host = (String) member.get("host").join();
		return ServiceInstance.builder().port(port).host(host != null ? host : getInstanceHost()).name(serviceId).id(member.id()).build();
	}

	/**
	 * Return a non loopback IPv4 address for the machine running this process.
	 * If the machine has multiple network interfaces, the IP address for the
	 * first interface returned by {@link java.net.NetworkInterface#getNetworkInterfaces} is returned.
	 *
	 * @return non loopback IPv4 address for the machine running this process
	 * @see java.net.NetworkInterface#getNetworkInterfaces
	 * @see java.net.NetworkInterface#getInetAddresses
	 */
	public static String getIpAddress() {
		try {
			for (Enumeration<NetworkInterface> enumNic = NetworkInterface.getNetworkInterfaces(); enumNic.hasMoreElements();) {
				NetworkInterface ifc = enumNic.nextElement();
				if (ifc.isUp()) {
					for (Enumeration<InetAddress> enumAddr = ifc.getInetAddresses(); enumAddr.hasMoreElements();) {
						InetAddress address = enumAddr.nextElement();
						if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
							return address.getHostAddress();
						}
					}
				}
			}
		} catch (IOException e) {
			// ignore
		}
		return "unknown";
	}
}
