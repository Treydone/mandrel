package io.mandrel.endpoints.rest;

import io.mandrel.task.TaskService;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/logs")
@RequestMapping(value = "/logs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
@Controller
@ResponseBody
public class LogResource {

	private final TaskService taskService;

	@Inject
	public LogResource(TaskService taskService) {
		super();
		this.taskService = taskService;
	}

	@ApiOperation(value = "List the loggers with their level")
	@RequestMapping
	public Map<String, String> all() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		return loggerContext.getLoggerList() != null ? loggerContext.getLoggerList().stream()
				.collect(Collectors.toMap(l -> l.getName(), l -> l.getLevel().toString())) : null;
	}

	@ApiOperation(value = "Change the log level")
	@RequestMapping(method = RequestMethod.POST)
	public void set(@RequestParam String logger, @RequestParam String level) {
		taskService.executeOnAllMembers(() -> {
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			loggerContext.getLogger(logger).setLevel(Level.toLevel(level.toUpperCase()));
		});
	}
}
