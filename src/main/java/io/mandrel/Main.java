package io.mandrel;

import io.mandrel.common.rest.ApiOriginFilter;
import io.mandrel.common.settings.Settings;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Main extends SpringBootServletInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	@Bean
	public ServletRegistrationBean cxfServlet() {
		org.apache.cxf.transport.servlet.CXFServlet cxfServlet = new org.apache.cxf.transport.servlet.CXFServlet();
		ServletRegistrationBean servletDef = new ServletRegistrationBean(
				cxfServlet, "/rest/*");
		servletDef.setLoadOnStartup(1);
		return servletDef;
	}

	@Bean
	public FilterRegistrationBean originFilter() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setFilter(new ApiOriginFilter());
		filterRegistrationBean.setUrlPatterns(Arrays.asList("/rest/*"));
		return filterRegistrationBean;
	}

	@Override
	protected SpringApplicationBuilder configure(
			SpringApplicationBuilder application) {
		return application.sources(Main.class);
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(
				Main.class, args);

		Settings settings = context.getBean(Settings.class);

		LOGGER.info("{} ({}) started", settings.getArtifact(),
				settings.getVersion());
	}

}
