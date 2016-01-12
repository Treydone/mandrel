package io.mandrel.common.thrift;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.facebook.swift.service.ThriftServerConfig;

@Component
@ConfigurationProperties(prefix = "thrift.server")
public class ThriftServerProperties extends ThriftServerConfig {

	private int port = 9090;

	@Min(0)
	@Max(65535)
	public int getPort() {
		return port;
	}

	public ThriftServerConfig setPort(int port) {
		this.port = port;
		return this;
	}
}
