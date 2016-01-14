package io.mandrel.common.thrift;

import static org.junit.Assert.assertNotNull;
import io.airlift.units.Duration;
import io.mandrel.common.settings.InfoSettings;
import io.mandrel.common.thrift.ConfigurationTest.TheConfiguration;
import io.mandrel.transport.thrift.ThriftServerConfiguration;
import io.mandrel.transport.thrift.ThriftTransportProperties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringApplicationConfiguration(classes = { TheConfiguration.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class ConfigurationTest {

	@Autowired
	private ThriftTransportProperties properties;

	@Autowired
	private InfoSettings settings;

	@Configuration
	@EnableConfigurationProperties({ ThriftTransportProperties.class, InfoSettings.class })
	@Import(ThriftServerConfiguration.class)
	public static class TheConfiguration {

	}

	@Test
	public void test() {

		System.err.println(ToStringBuilder.reflectionToString(properties));
		assertNotNull(settings.getVersion());
		Assertions.assertThat(properties.getPort()).isEqualTo(8888);
		Assertions.assertThat(properties.getIdleConnectionTimeout()).isEqualTo(Duration.valueOf("6s"));

	}
}