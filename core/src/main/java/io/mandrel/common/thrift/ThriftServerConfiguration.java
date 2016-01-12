package io.mandrel.common.thrift;

import io.airlift.units.Duration;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class ThriftServerConfiguration {

	@Bean
	@ConfigurationPropertiesBinding
	public StringToDurationConverter stringToDurationConverter() {
		return new StringToDurationConverter();
	}

	private static class StringToDurationConverter implements Converter<String, Duration> {

		@Override
		public Duration convert(String source) {
			return Duration.valueOf(source);
		}
	}
}
