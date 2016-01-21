package io.mandrel.cluster.discovery.local;

import io.mandrel.cluster.discovery.DiscoveryClient;
import io.mandrel.cluster.discovery.DiscoveryProperties;
import io.mandrel.cluster.discovery.ServiceInstance;

import java.util.Arrays;
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
		ServiceInstance finalInstance = ServiceInstance.builder().port(instance.getPort())
				.host(instance.getHost() != null ? instance.getHost() : getInstanceHost()).name(instance.getName()).id(ID).build();
		services.put(instance.getName(), finalInstance);
		return finalInstance;
	}

	@Override
	public void unregister(String serviceId) {
		services.remove(serviceId);
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		return Arrays.asList(services.get(serviceId));
	}

	@Override
	public List<String> getServices() {
		return Lists.newArrayList(services.keySet());
	}

	@Override
	public String getInstanceHost() {
		String host = discoveryProperties.getInstanceHost() == null ? "localhost" : discoveryProperties.getInstanceHost();
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

}
