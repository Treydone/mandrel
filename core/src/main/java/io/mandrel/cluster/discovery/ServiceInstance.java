package io.mandrel.cluster.discovery;

import com.google.common.net.HostAndPort;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class ServiceInstance {
	private final String name;
	private final String host;
	private final Integer port;

	public HostAndPort getHostAndPort() {
		return HostAndPort.fromParts(this.getHost(), this.getPort());
	}
}
