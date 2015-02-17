package io.mandrel.service.stats;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Component
public class StatsService {

	private final HazelcastInstance instance;

	@Inject
	public StatsService(HazelcastInstance instance) {
		this.instance = instance;
	}

	public Stats get(long spiderId) {
		return new Stats();
	}

	public void prepare(long spiderId) {
		stats(spiderId);
	}

	private IMap<Object, Object> stats(long spiderId) {
		return instance.getMap("stats-" + spiderId);
	}
}
