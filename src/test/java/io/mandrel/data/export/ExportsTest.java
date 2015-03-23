package io.mandrel.data.export;

import static org.junit.Assert.assertEquals;
import io.mandrel.config.BindConfiguration;
import io.mandrel.data.export.ExportsTest.LocalConfiguration;

import java.io.IOException;

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
public class ExportsTest {

	@Configuration
	@Import(BindConfiguration.class)
	public static class LocalConfiguration {

	}

	@Inject
	private ObjectMapper objectMapper;

	@Test
	public void delimited() throws IOException {

		DelimiterSeparatedValuesExporter exporter = new DelimiterSeparatedValuesExporter();

		String json = objectMapper.writeValueAsString(exporter);
		System.err.println(json);
		Exporter read = objectMapper.readValue(json, Exporter.class);
		assertEquals(exporter, read);
	}

	@Test
	public void json() throws IOException {

		JsonExporter exporter = new JsonExporter();

		String json = objectMapper.writeValueAsString(exporter);
		System.err.println(json);
		Exporter read = objectMapper.readValue(json, Exporter.class);
		assertEquals(exporter, read);
	}

}
