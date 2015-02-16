package io.mandrel.rest;

import io.mandrel.service.task.TaskService;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api("/log")
@Path("/log")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class LogResource {

	private final TaskService taskService;

	@Inject
	public LogResource(TaskService taskService) {
		super();
		this.taskService = taskService;
	}

	@ApiOperation(value = "List the loggers with their level")
	@GET
	public Map<String, String> all() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		return loggerContext.getLoggerList() != null ? loggerContext.getLoggerList().stream()
				.collect(Collectors.toMap(l -> l.getName(), l -> l.getLevel().toString())) : null;
	}

	@ApiOperation(value = "Change the log level")
	@POST
	public void set(@QueryParam("logger") String logger, @QueryParam("level") String level) {
		taskService.executeOnAllMembers(() -> {
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			loggerContext.getLogger(logger).setLevel(Level.toLevel(level.toUpperCase()));
		});
	}
}
