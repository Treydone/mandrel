package io.mandrel.cluster.discovery;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class ServiceInstance {
	private final String name;
	private final String host;
	private final Integer port;
}
