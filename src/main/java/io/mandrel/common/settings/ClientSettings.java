package io.mandrel.common.settings;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "client")
@Data
public class ClientSettings {

	@Data
	public static class Timeouts {
		private int request;
		private int connection;
	}

	@Data
	public static class Connections {
		private int host = -1;
		private int global = -1;
	}

	private Timeouts timouts = new Timeouts();
	private Connections connections = new Connections();

}
