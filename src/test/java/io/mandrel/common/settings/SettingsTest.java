package io.mandrel.common.settings;

import static org.junit.Assert.*;
import io.mandrel.common.settings.SettingsTest.SettingsConfiguration;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = SettingsConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SettingsTest {

	@Configuration
	@EnableConfigurationProperties(InfoSettings.class)
	public static class SettingsConfiguration {

	}

	@Inject
	private InfoSettings settings;

	@Test
	public void test() {

		assertNotNull(settings.getVersion());

	}
}
