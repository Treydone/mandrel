package io.mandrel.service.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.common.data.State;
import io.mandrel.common.source.Source;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

@Component
@Slf4j
public class SpiderService {

	private final HazelcastInstance instance;

	private Validator spiderValidator = new SpiderValidator();

	@Inject
	public SpiderService(HazelcastInstance instance) {
		this.instance = instance;
	}

	public Errors validate(Spider spider) {
		Errors errors = new BeanPropertyBindingResult(spider, "spider");
		spiderValidator.validate(spider, errors);
		return errors;
	}

	/**
	 * Store the spider and distribute it across the cluster.
	 * 
	 * @param spider
	 * @return
	 */
	public Spider add(Spider spider) {

		Errors errors = validate(spider);

		if (errors.hasErrors()) {
			// TODO
			throw new RuntimeException("Errors!");
		}

		long id = instance.getIdGenerator("spiders").newId();
		spider.setId(id);
		spiders(instance).put(id, spider);
		return spider;
	}

	public Optional<Spider> get(long id) {
		Spider value = spiders(instance).get(id);
		return value == null ? Optional.empty() : Optional.of(value);
	}

	public Stream<Spider> list() {
		return spiders(instance).values().stream().map(el -> (Spider) el);
	}

	public void start(long spiderId) {
		get(spiderId).map(spider -> {

			// Prepare spider executor
				instance.getConfig().getExecutorConfig("executor-" + spiderId).setPoolSize(1).setStatisticsEnabled(true).setQueueCapacity(1);

				spider.getSources().stream().filter(s -> s.check()).forEach(prepareSource(spiderId, spider));

				// Gooooo
				instance.getExecutorService("executor-" + spiderId).executeOnAllMembers(new SpiderTask(spider));

				// Update status
				spider.setState(State.STARTED);
				spiders(instance).put(spiderId, spider);
				return spider;

			}).orElseThrow(() -> new RuntimeException("Unknown spider!"));

	}

	private Consumer<? super Source> prepareSource(long spiderId, Spider spider) {
		return source -> {
			String sourceExecServiceName = "executor-" + spiderId + "-source-" + source.getName();

			instance.getConfig().getExecutorConfig(sourceExecServiceName).setPoolSize(1).setStatisticsEnabled(true).setQueueCapacity(1);

			if (source.singleton()) {
				log.debug("Sourcing from a random member");
				instance.getExecutorService(sourceExecServiceName).execute(new SourceTask(spider, source), ms -> true);
			} else {
				log.debug("Sourcing from all members");
				instance.getExecutorService(sourceExecServiceName).executeOnAllMembers(new SourceTask(spider, source));
			}
		};
	}

	public void cancel(long spiderId) {
		get(spiderId).map(spider -> {

			// Shutdown source execution service
				spider.getSources().stream().forEach(source -> {
					String sourceExecServiceName = "executor-" + spiderId + "-source-" + source.getName();
					shutdownExecutorService(sourceExecServiceName);
				});

				// Shutdown task execution service
				String taskExecServiceName = "executor-" + spiderId;
				shutdownExecutorService(taskExecServiceName);

				// Update status
				spider.setState(State.CANCELLED);
				return spiders(instance).put(spiderId, spider);
			}).orElseThrow(() -> new RuntimeException("Unknown spider!"));

	}

	private void shutdownExecutorService(String taskExecServiceName) {
		IExecutorService pool = instance.getExecutorService(taskExecServiceName);
		pool.shutdown();
		try {
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					log.debug("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
		pool.destroy();
	}

	// ------------------------------ TOOLS

	static Map<Long, Spider> spiders(HazelcastInstance instance) {
		return instance.getReplicatedMap("spiders");
	}

}
