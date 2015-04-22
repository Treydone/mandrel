package io.mandrel.common.settings;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "info")
@Data
public class InfoSettings {

	private String artifact;
	private String version;
	private String name;
	private String description;

}
