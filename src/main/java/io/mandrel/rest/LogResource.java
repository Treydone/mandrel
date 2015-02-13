package io.mandrel.rest;

import io.mandrel.service.node.NodeService;

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

	private final NodeService nodeService;

	@Inject
	public LogResource(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@ApiOperation(value = "List the loggers with their level")
	@GET
	public Map<String, String> all() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		return loggerContext.getLoggerList().stream().collect(Collectors.toMap(l -> l.getName(), l -> l.getLevel().toString()));
	}

	@ApiOperation(value = "Change the log level")
	@POST
	public void set(@QueryParam("logger") String logger, @QueryParam("level") String level) {
		nodeService.executeOnAllMembers(() -> {
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			loggerContext.getLogger(logger).setLevel(Level.toLevel(level.toUpperCase()));
		});
	}
}
