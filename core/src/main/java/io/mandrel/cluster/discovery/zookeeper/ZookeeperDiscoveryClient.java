package io.mandrel.cluster.discovery.zookeeper;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;
import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.DiscoveryProperties;
import io.mandrel.cluster.discovery.ServiceInstance;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import lombok.SneakyThrows;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.UriSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(value = "discovery.zookeeper.enabled", matchIfMissing = true)
@Component
public class ZookeeperDiscoveryClient implements DiscoveryClient {

	@Autowired
	private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	@Autowired
	private DiscoveryProperties discoveryProperties;
	@Autowired
	private ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;

	@Autowired
	private ApplicationContext context;

	@Override
	@SneakyThrows
	public ServiceInstance register(ServiceInstance instance) {
		org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> service = createService(instance);
		serviceDiscovery.registerService(service);
		return create(service);
	}

	public org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> createService(ServiceInstance instance) throws Exception {
		String host = getInstanceHost();

		org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> service = org.apache.curator.x.discovery.ServiceInstance
				.<ZookeeperInstance> builder().name(instance.getName()).payload(new ZookeeperInstance(context.getId())).port(instance.getPort()).address(host)
				.uriSpec(new UriSpec(zookeeperDiscoveryProperties.getUriSpec())).build();
		return service;
	}

	public String getInstanceHost() {
		String host = discoveryProperties.getInstanceHost() == null ? getIpAddress() : discoveryProperties.getInstanceHost();
		return host;
	}

	@Override
	@SneakyThrows
	public void unregister(ServiceInstance instance) {
		org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> service = createService(instance);
		serviceDiscovery.unregisterService(service);
	}

	@Override
	@SneakyThrows
	public List<ServiceInstance> getInstances(String serviceId) {
		Collection<org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance>> zkInstances = serviceDiscovery.queryForInstances(serviceId);

		List<ServiceInstance> instances = new ArrayList<>(zkInstances.size());

		for (org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> instance : zkInstances) {
			instances.add(create(instance));
		}

		return instances;
	}

	public ServiceInstance create(org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> instance) {
		return ServiceInstance.builder().host(instance.getAddress()).port(instance.getPort()).name(instance.getName()).build();
	}

	@Override
	public List<String> getServices() {
		List<String> services = null;
		try {
			services = new ArrayList<>(serviceDiscovery.queryForNames());
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
		return services;
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
