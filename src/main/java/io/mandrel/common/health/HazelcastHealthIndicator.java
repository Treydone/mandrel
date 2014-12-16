package io.mandrel.common.health;

import javax.inject.Inject;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
public class HazelcastHealthIndicator extends AbstractHealthIndicator {

	private final HazelcastInstance hazelcastInstance;

	@Inject
	public HazelcastHealthIndicator(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		try {
			hazelcastInstance.getCluster();
			builder.up();
		} catch (Exception e) {
			builder.down(e);
		}
	}
}
