package io.mandrel.service.spider;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

@Component
public class SpiderService {

	private final HazelcastInstance instance;

	@Inject
	public SpiderService(HazelcastInstance instance) {
		this.instance = instance;
	}

	public Spider add(Spider spider) {
		long id = instance.getIdGenerator("spiders").newId();
		spider.setId(id);

		// TODO: test datastore connectivity
		if (spider.getExtractors() != null) {
			spider.getExtractors().stream().map(ex -> ex.getDataStore())
					.filter(ds -> ds != null).forEach(ds -> {
						ds.check();
					});
		}

		// Test sources
		Map<String, Boolean> checks = spider
				.getSources()
				.stream()
				.collect(
						Collectors.toMap(s -> s.getClass().getName(),
								s -> s.check()));
		boolean check = checks.entrySet().stream()
				.anyMatch(entry -> entry.getValue());
		if (check) {
			// TODO Throw STG
		}

		// spider.getSources().stream().parallel().forEach(s -> s);

		instance.getMap("spiders").put(id, spider);
		return spider;
	}

	public void update(Spider spider) {
		instance.getMap("spiders").put(spider.getId(), spider);
	}

	public Optional<Spider> get(long id) {
		Spider value = (Spider) instance.getMap("spiders").get(id);
		return value == null ? Optional.empty() : Optional.of(value);
	}

	public Stream<Spider> list() {
		return instance.getMap("spiders").values().stream()
				.map(el -> (Spider) el);
	}

	public void start(Spider spider) {

		// TODO
		Map<String, Object> properties = null;

		spider.getSources().stream().forEach(source -> {
			source.init(properties);
		});

		spider.setState(State.STARTED);
		update(spider);

	}
}
