package io.mandrel.transport.thrift;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.facebook.swift.service.ThriftServerConfig;

@Component
@ConfigurationProperties(prefix = "transport.thrift")
public class ThriftTransportProperties extends ThriftServerConfig {
}
