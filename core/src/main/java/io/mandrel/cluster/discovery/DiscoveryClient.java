package io.mandrel.cluster.discovery;

import java.util.List;

public interface DiscoveryClient {

	void register(ServiceInstance instance);

	void unregister(ServiceInstance instance);

	List<ServiceInstance> getInstances(String serviceId);

	List<String> getServices();

}
