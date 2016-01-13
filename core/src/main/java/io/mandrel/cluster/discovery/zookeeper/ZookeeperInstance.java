package io.mandrel.cluster.discovery.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ZookeeperInstance {
	private String id;

	@SuppressWarnings("unused")
	private ZookeeperInstance() {
	}
}
