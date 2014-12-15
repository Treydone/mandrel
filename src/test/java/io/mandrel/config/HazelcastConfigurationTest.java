package io.mandrel.config;

import static org.junit.Assert.assertNotNull;
import io.mandrel.common.settings.NetworkSettings;
import io.mandrel.config.HazelcastConfigurationTest.LocalConfiguration;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = LocalConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class HazelcastConfigurationTest {

	@Configuration
	@EnableConfigurationProperties(NetworkSettings.class)
	@Import(HazelcastConfiguration.class)
	public static class LocalConfiguration {

	}

	@Inject
	private NetworkSettings networkSettings;

	@Test
	public void test() {

		assertNotNull(networkSettings.getInterfaces());
		assertNotNull(networkSettings.getGroup());
		assertNotNull(networkSettings.getGroup().getName());

	}
}