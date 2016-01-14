package io.mandrel.cluster.discovery;

import java.util.List;

public interface DiscoveryClient {

	ServiceInstance register(ServiceInstance instance);

	void unregister(ServiceInstance instance);

	List<ServiceInstance> getInstances(String serviceId);

	List<String> getServices();

	String getInstanceHost();
}
