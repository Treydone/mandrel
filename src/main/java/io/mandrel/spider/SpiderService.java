package io.mandrel.spider;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Resource;

import com.hazelcast.core.HazelcastInstance;

@Resource
public class SpiderService {

	private final HazelcastInstance instance;

	public SpiderService(HazelcastInstance instance) {
		this.instance = instance;
	}

	public void add(Spider spider) {
		long id = instance.getIdGenerator("spiders").newId();
		instance.getMap("spiders").put(id, spider);
	}

	public Optional<Spider> get(long id) {
		return Optional.of((Spider) instance.getMap("spiders").get(id));
	}

	public Stream<Spider> list() {
		return instance.getMap("spiders").values().stream()
				.map(el -> (Spider) el);
	}
}
