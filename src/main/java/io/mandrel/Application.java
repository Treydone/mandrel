package io.mandrel;

import io.mandrel.common.rest.ApiOriginFilter;
import io.mandrel.common.settings.Settings;

import java.util.Arrays;

import org.jhades.JHades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.actuate.system.EmbeddedServerPortFileWriter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application extends SpringBootServletInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

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

		Settings settings = context.getBean(Settings.class);

		LOGGER.info("{} ({}) started", settings.getArtifact(), settings.getVersion());
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
