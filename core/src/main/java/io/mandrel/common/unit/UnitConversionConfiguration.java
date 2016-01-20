package io.mandrel.common.unit;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class UnitConversionConfiguration {

	@Bean
	@ConfigurationPropertiesBinding
	public StringToTimeValueConverter stringToDurationConverter() {
		return new StringToTimeValueConverter();
	}

	private static class StringToTimeValueConverter implements Converter<String, TimeValue> {

		@Override
		public TimeValue convert(String source) {
			return TimeValue.parseTimeValue(source);
		}
	}
}
