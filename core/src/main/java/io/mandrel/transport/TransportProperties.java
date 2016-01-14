package io.mandrel.transport;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "transport")
public class TransportProperties {

	private int port = 9090;
	private String bindAddress = "localhost";

	@Min(0)
	@Max(65535)
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
