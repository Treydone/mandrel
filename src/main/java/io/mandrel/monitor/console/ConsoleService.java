package io.mandrel.monitor.console;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

@Component
public class ConsoleService {

	private final Environment env;

	private final SimpMessageSendingOperations simp;

	@Autowired
	public ConsoleService(Environment environment, SimpMessageSendingOperations simp) {
		this.env = environment;
		this.simp = simp;
	}

	@PostConstruct
	public void init() {
		Boolean isConsoleEnabled = env.getProperty("logging.console.enabled", Boolean.class, false);
		if (isConsoleEnabled) {
			String pattern = env.getRequiredProperty("logging.console.pattern", String.class);
			String level = env.getProperty("logging.console.level", String.class, "WARN");

			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

			ConsoleAppender appender = new ConsoleAppender();
			appender.setContext(loggerContext);
			appender.setPattern(pattern);
			appender.setMessagingTemplate(simp);
			appender.start();

			Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			logger.addAppender(appender);
			logger.setLevel(Level.valueOf(level));
			logger.setAdditive(false);
		}
	}
}
