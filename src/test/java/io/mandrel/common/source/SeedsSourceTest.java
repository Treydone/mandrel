package io.mandrel.common.source;

import static org.junit.Assert.assertEquals;
import io.mandrel.common.source.SeedsSourceTest.LocalConfiguration;
import io.mandrel.config.BindConfiguration;

import java.io.IOException;
import java.util.Arrays;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = LocalConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SeedsSourceTest {

	@Configuration
	@Import(BindConfiguration.class)
	public static class LocalConfiguration {

	}

	@Inject
	private ObjectMapper objectMapper;

	@Test
	public void seed() throws IOException {

		SeedsSource source = new SeedsSource();
		source.setSeeds(Arrays.asList("test"));

		String json = objectMapper.writeValueAsString(source);
		System.err.println(json);
		Source read = objectMapper.readValue(json, Source.class);
		assertEquals(source, read);
	}

	@Test
	public void jdbc() throws IOException {

		JdbcSource source = new JdbcSource();
		source.setQuery("select * from test");
		source.setUrl("url");

		String json = objectMapper.writeValueAsString(source);
		System.err.println(json);
		Source read = objectMapper.readValue(json, Source.class);
		assertEquals(source, read);
	}

	@Test
	public void jms() throws IOException {

		JmsSource source = new JmsSource();
		source.setUrl("url");

		String json = objectMapper.writeValueAsString(source);
		System.err.println(json);
		Source read = objectMapper.readValue(json, Source.class);
		assertEquals(source, read);
	}
}
