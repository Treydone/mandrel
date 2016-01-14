package io.mandrel.cluster.discovery.zookeeper;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("discovery.zookeeper")
@Data
public class ZookeeperDiscoveryProperties {

	private boolean enabled = true;

	private String root = "/mandrel";

	private String uriSpec = "{scheme}://{address}:{port}";

	@NotNull
	private String connectString = "localhost:2181";

	/**
	 * @param baseSleepTimeMs
	 *            initial amount of time to wait between retries
	 */
	private Integer baseSleepTimeMs = 50;

	/**
	 * @param maxRetries
	 *            max number of times to retry
	 */
	private Integer maxRetries = 10;

	/**
	 * @param maxSleepMs
	 *            max time in ms to sleep on each retry
	 */
	private Integer maxSleepMs = 500;

	private Integer blockUntilConnectedWait = 10;

	private TimeUnit blockUntilConnectedUnit = TimeUnit.SECONDS;
}
