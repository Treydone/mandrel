package io.mandrel.data.spider;

import io.mandrel.cluster.idgenerator.IdGenerator;
import io.mandrel.common.data.Spider;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpiderRepository {

	private final IdGenerator idGenerator;

	private final HazelcastInstance instance;

	public Spider add(Spider spider) {
		long id = idGenerator.generateId("spiders");
		spider.setId(id);
		spiders(instance).put(id, spider);
		return spider;
	}

	public Spider update(Spider spider) {
		spiders(instance).put(spider.getId(), spider);
		return spider;
	}

	public void delete(long id) {
		spiders(instance).remove(id);
	}

	public Optional<Spider> get(long id) {
		Spider value = spiders(instance).get(id);
		return value == null ? Optional.empty() : Optional.of(value);
	}

	public Stream<Spider> list() {
		return spiders(instance).values().stream().map(el -> el);
	}

	// ------------------------------ TOOLS

	static Map<Long, Spider> spiders(HazelcastInstance instance) {
		return instance.getReplicatedMap("spiders");
	}

}
