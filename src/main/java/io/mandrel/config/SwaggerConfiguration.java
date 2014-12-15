package io.mandrel.config;

import io.mandrel.common.settings.Settings;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wordnik.swagger.jaxrs.config.BeanConfig;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;

@Configuration
public class SwaggerConfiguration {

	@Bean
	public BeanConfig beanConfig(Settings settings) {
		BeanConfig config = new BeanConfig();
		config.setVersion(settings.getVersion());
		config.setDescription(settings.getDescription());
		config.setTitle(settings.getName() + "(" + settings.getArtifact() + ")");
		config.setBasePath("/rest");
		config.setResourcePackage("cbm.api");
		config.setScan(true);
		return config;
	}

	// Swagger API listing resource
	@Bean
	public ApiListingResourceJSON apiListingResource() {
		return new ApiListingResourceJSON();
	}

	// Swagger writers
	@Bean
	public ResourceListingProvider resourceListingProvider() {
		return new ResourceListingProvider();
	}

	@Bean
	public ApiDeclarationProvider apiDeclarationProvider() {
		return new ApiDeclarationProvider();
	}
}
