package io.mandrel.common.settings;

import static org.junit.Assert.*;
import io.mandrel.common.settings.NetworkSettingsTest.NetworkSettingsConfiguration;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = NetworkSettingsConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class NetworkSettingsTest {

	@Configuration
	@EnableConfigurationProperties(NetworkSettings.class)
	public static class NetworkSettingsConfiguration {

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
