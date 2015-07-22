package io.mandrel.config;

import io.mandrel.common.data.Strategy;
import io.mandrel.common.settings.ClientSettings;
import io.mandrel.http.HCRequester;
import io.mandrel.http.Requester;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpConfiguration {

	@Bean
	public Requester defaultRequester(ClientSettings settings) {
		HCRequester hcRequester = new HCRequester();
		hcRequester.setSettings(settings);
		hcRequester.setStrategy(new Strategy());
		hcRequester.init();
		return hcRequester;
	}
}
