package io.mandrel.bootstrap.config;

import java.util.Map;

import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.actuate.system.EmbeddedServerPortFileWriter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.ConfigServerApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Maps;

@Configuration
@EnableAutoConfiguration
@EnableConfigServer
public class ConfigServer {

	public static void main(String[] args) {
		Map<String, Object> properties = Maps.newHashMap();
		properties.put("spring.profiles.active", "native");
		properties.put("spring.config.location", "classpath:/version.yml,classpath:/configsrv.yml");

		new SpringApplicationBuilder(ConfigServerApplication.class).properties(properties)
				.listeners(new ApplicationPidFileWriter(), new EmbeddedServerPortFileWriter()).run(args);
	}
}
