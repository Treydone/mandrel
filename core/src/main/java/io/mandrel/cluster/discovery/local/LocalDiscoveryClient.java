package io.mandrel.cluster.discovery.local;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.DiscoveryProperties;
import io.mandrel.cluster.discovery.ServiceInstance;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@ConditionalOnProperty(value = "discovery.local.enabled", matchIfMissing = false)
@Component
public class LocalDiscoveryClient implements DiscoveryClient {

	private final static String ID = UUID.randomUUID().toString();

	@Autowired
	private DiscoveryProperties discoveryProperties;

	private ConcurrentMap<String, ServiceInstance> services = new ConcurrentHashMap<>();

	@Override
	public ServiceInstance register(ServiceInstance instance) {
		ServiceInstance finalInstance = ServiceInstance.builder().port(instance.getPort()).host(getInstanceHost()).name(instance.getName()).id(ID).build();
		services.put(instance.getName(), finalInstance);
		return finalInstance;
	}

	@Override
	public void unregister(String serviceId) {
		services.remove(serviceId);
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		return Lists.newArrayList(services.values());
	}

	@Override
	public List<String> getServices() {
		return Lists.newArrayList(services.keySet());
	}

	@Override
	public String getInstanceHost() {
		String host = discoveryProperties.getInstanceHost() == null ? getIpAddress() : discoveryProperties.getInstanceHost();
		return host;
	}

	@Override
	public ServiceInstance getLocalInstance(String serviceId) {
		return services.get(serviceId);
	}

	@Override
	public ServiceInstance getInstance(String id, String serviceId) {
		return services.get(serviceId);
	}

	@Override
	public String getInstanceId() {
		return ID;
	}

	/**
	 * Return a non loopback IPv4 address for the machine running this process.
	 * If the machine has multiple network interfaces, the IP address for the
	 * first interface returned by
	 * {@link java.net.NetworkInterface#getNetworkInterfaces} is returned.
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
