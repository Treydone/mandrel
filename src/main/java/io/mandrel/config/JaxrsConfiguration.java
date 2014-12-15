package io.mandrel.config;

import io.mandrel.common.jaxrs.ValidationExceptionMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationOutInterceptor;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;

@Configuration
public class JaxrsConfiguration {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JaxrsConfiguration.class);

	@Bean(destroyMethod = "shutdown")
	public Bus cxf() {
		return new SpringBus();
	}

	@Bean
	@DependsOn("cxf")
	public Server server(ApplicationContext context) {
		JAXRSServerFactoryBean sf = RuntimeDelegate.getInstance()
				.createEndpoint(jaxRsApiApplication(),
						JAXRSServerFactoryBean.class);

		List<Object> servicesBean = new ArrayList<Object>(context
				.getBeansWithAnnotation(Path.class).values());
		LOGGER.info("Adding services {}", servicesBean);
		sf.setServiceBeans(servicesBean);

		sf.setInInterceptors(Arrays.<Interceptor<? extends Message>> asList(
				new LoggingInInterceptor(), validationInInterceptor()));
		sf.setOutInterceptors(Arrays.<Interceptor<? extends Message>> asList(
				new LoggingOutInterceptor(), validationOutInterceptor()));
		sf.setBus(cxf());

		sf.setAddress("/");

		// Swagger is in
		sf.setProviders(Arrays.asList(
				context.getBean(ResourceListingProvider.class),
				context.getBean(ApiDeclarationProvider.class),
				context.getBean(JacksonJsonProvider.class),
				new ValidationExceptionMapper()));

		return sf.create();
	}

	@Bean
	public JAXRSBeanValidationInInterceptor validationInInterceptor() {
		return new JAXRSBeanValidationInInterceptor();
	}

	@Bean
	public JAXRSBeanValidationOutInterceptor validationOutInterceptor() {
		return new JAXRSBeanValidationOutInterceptor();
	}

	@Bean
	public JaxRsApiApplication jaxRsApiApplication() {
		return new JaxRsApiApplication();
	}

	static class JaxRsApiApplication extends Application {

	}
}
