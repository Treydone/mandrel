package io.mandrel.service.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.common.data.State;
import io.mandrel.common.source.Source;
import io.mandrel.service.task.TaskService;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@Slf4j
public class SpiderService {

	private final SpiderRepository spiderRepository;

	private final TaskService taskService;

	private Validator spiderValidator = new SpiderValidator();

	@Inject
	public SpiderService(SpiderRepository spiderRepository, TaskService taskService) {
		this.spiderRepository = spiderRepository;
		this.taskService = taskService;
	}

	@PostConstruct
	public void init() {

		// Start the available spiders on this node
		spiderRepository.list().filter(spider -> State.STARTED.equals(spider.getState())).forEach(spider -> {
			taskService.executeOnLocalMember(String.valueOf(spider.getId()), new SpiderTask(spider));

			// TODO manage sources!
			// spider.getSources().stream().forEach(prepareSource(spider.getId(),
			// spider));
			});
	}

	public Errors validate(Spider spider) {
		Errors errors = new BeanPropertyBindingResult(spider, "spider");
		spiderValidator.validate(spider, errors);
		return errors;
	}

	public Spider add(Spider spider) {

		Errors errors = validate(spider);

		if (errors.hasErrors()) {
			// TODO
			throw new RuntimeException("Errors!");
		}

		return spiderRepository.add(spider);
	}

	public Optional<Spider> get(long id) {
		return spiderRepository.get(id);
	}

	public Stream<Spider> list() {
		return spiderRepository.list();
	}

	public void start(long spiderId) {
		get(spiderId).map(spider -> {

			if (State.STARTED.equals(spider.getState())) {
				return spider;
			}

			if (State.CANCELLED.equals(spider.getState())) {
				throw new RuntimeException("Spider cancelled!");
			}

			spider.getSources().stream().filter(s -> s.check()).forEach(prepareSource(spiderId, spider));

			taskService.prepareSimpleExecutor(String.valueOf(spiderId));
			taskService.executeOnAllMembers(String.valueOf(spiderId), new SpiderTask(spider));

			spider.setState(State.STARTED);
			spiderRepository.update(spider);
			return spider;

		}).orElseThrow(() -> new RuntimeException("Unknown spider!"));
	}

	private Consumer<? super Source> prepareSource(long spiderId, Spider spider) {
		return source -> {
			String sourceExecServiceName = spiderId + "-source-" + source.getName();

			taskService.prepareSimpleExecutor(String.valueOf(sourceExecServiceName));

			if (source.singleton()) {
				log.debug("Sourcing from a random member");
				taskService.executeOnRandomMember(sourceExecServiceName, new SourceTask(spider, source));
			} else {
				log.debug("Sourcing from all members");
				taskService.executeOnAllMembers(sourceExecServiceName, new SourceTask(spider, source));
			}
		};
	}

	public void cancel(long spiderId) {
		get(spiderId).map(spider -> {

			// Shutdown source execution service
				spider.getSources().stream().forEach(source -> {
					String sourceExecServiceName = "executor-" + spiderId + "-source-" + source.getName();
					taskService.shutdownExecutorService(sourceExecServiceName);
				});

				// Shutdown task execution service
				String taskExecServiceName = "executor-" + spiderId;
				taskService.shutdownExecutorService(taskExecServiceName);

				// Update status
				spider.setState(State.CANCELLED);
				return spiderRepository.update(spider);
			}).orElseThrow(() -> new RuntimeException("Unknown spider!"));

	}

}
