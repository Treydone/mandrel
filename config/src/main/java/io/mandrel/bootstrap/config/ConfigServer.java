package io.mandrel.bootstrap.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.actuate.system.EmbeddedServerPortFileWriter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
// @EnableEurekaServer
@EnableConfigServer
public class ConfigServer {

	public static void main(String[] args) {
		Map<String, Object> properties = new HashMap<>();
		// properties.put("debug", "true");
		properties.put("spring.profiles.active", "native");
		properties.put("spring.config.location", "classpath:/version.yml,classpath:/configsrv.yml");

		new SpringApplicationBuilder(ConfigServer.class).properties(properties).listeners(new ApplicationPidFileWriter(), new EmbeddedServerPortFileWriter())
				.run(args);
	}
}
