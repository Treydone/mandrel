package io.mandrel.monitor;

import io.mandrel.monitor.SigarService;
import io.mandrel.monitor.SigarServiceTest.LocalConfiguration;

import javax.inject.Inject;

import org.hyperic.sigar.SigarException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = LocalConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SigarServiceTest {

	@Configuration
	public static class LocalConfiguration {

		@Bean
		public SigarService sigarService(ResourceLoader loader) {
			return new SigarService(loader);
		}
	}

	@Inject
	private SigarService sigarService;

	@Test
	public void test() throws SigarException {

		System.err.println(sigarService.infos());

	}
}
