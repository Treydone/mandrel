package io.mandrel.stats;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
public class StatsService {

	private final HazelcastInstance instance;

	@Inject
	public StatsService(HazelcastInstance instance) {
		this.instance = instance;
	}

	public Stats get(long spiderId) {
		return new Stats(instance, spiderId);
	}

	public void delete(long spiderId) {
		get(spiderId).delete();
	}
}
