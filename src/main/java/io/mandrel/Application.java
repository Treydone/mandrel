package io.mandrel;

import io.mandrel.common.console.ConsoleAppender;
import io.mandrel.common.rest.ApiOriginFilter;
import io.mandrel.common.settings.InfoSettings;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.jhades.JHades;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.actuate.system.EmbeddedServerPortFileWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

@SpringBootApplication
@Slf4j
public class Application extends SpringBootServletInitializer {

	private ConfigurableApplicationContext context;

	@Bean
	public FilterRegistrationBean originFilter() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setFilter(new ApiOriginFilter());
		filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));
		return filterRegistrationBean;
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {
		new Application().start(args);
	}

	public void start(String[] args) {

		// Print some useful infos about the classpath and others things
		new JHades().overlappingJarsReport();

		context = SpringApplication.run(Application.class, args);
		context.addApplicationListener(new ApplicationPidFileWriter());
		context.addApplicationListener(new EmbeddedServerPortFileWriter());

		InfoSettings settings = context.getBean(InfoSettings.class);

		log.info("{} ({}) started", settings.getArtifact(), settings.getVersion());

		// Log console configuration
		Environment env = context.getBean(Environment.class);
		Boolean isConsoleEnabled = env.getProperty("logging.console.enabled", Boolean.class, false);
		if (isConsoleEnabled) {
			String pattern = env.getRequiredProperty("logging.console.pattern", String.class);
			String level = env.getProperty("logging.console.level", String.class, "WARN");

			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

			SimpMessageSendingOperations messagingTemplate = context.getBean(SimpMessageSendingOperations.class);

			ConsoleAppender appender = new ConsoleAppender();
			appender.setContext(loggerContext);
			appender.setPattern(pattern);
			appender.setMessagingTemplate(messagingTemplate);
			appender.start();

			Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			logger.addAppender(appender);
			logger.setLevel(Level.valueOf(level));
			logger.setAdditive(false);
		}
	}

	public void stop() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500L);
				} catch (InterruptedException ex) {
					// Swallow exception and continue
				}
				context.close();
			}
		}).start();
	}
}
