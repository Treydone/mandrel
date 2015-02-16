package io.mandrel.service.spider;

import io.mandrel.common.data.Spider;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
public class SpiderRepository {

	private final HazelcastInstance instance;

	@Inject
	public SpiderRepository(HazelcastInstance instance) {
		this.instance = instance;
	}

	public Spider add(Spider spider) {
		long id = instance.getIdGenerator("spiders").newId();
		spider.setId(id);
		spiders(instance).put(id, spider);
		return spider;
	}

	public Spider update(Spider spider) {
		spiders(instance).put(spider.getId(), spider);
		return spider;
	}

	public Optional<Spider> get(long id) {
		Spider value = spiders(instance).get(id);
		return value == null ? Optional.empty() : Optional.of(value);
	}

	public Stream<Spider> list() {
		return spiders(instance).values().stream().map(el -> (Spider) el);
	}

	// ------------------------------ TOOLS

	static Map<Long, Spider> spiders(HazelcastInstance instance) {
		return instance.getReplicatedMap("spiders");
	}

}
