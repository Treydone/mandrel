package io.mandrel.cluster.discovery;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("discovery")
@Data
public class DiscoveryProperties {

	private String instanceHost;

}
