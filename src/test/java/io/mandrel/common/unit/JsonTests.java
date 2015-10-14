package io.mandrel.common.unit;

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
public class JsonTests {

	@Configuration
	@Import(BindConfiguration.class)
	public static class LocalConfiguration {

	}

	@Inject
	private ObjectMapper objectMapper;

	@Test
	public void byteSizeValue() throws IOException {

		ByteSizeValue value = new ByteSizeValue(48000);

		String json = objectMapper.writeValueAsString(value);
		System.err.println(json);
		ByteSizeValue read = objectMapper.readValue(json, ByteSizeValue.class);
		assertEquals(value.getKb(), read.getKb());
	}

	@Test
	public void timeValue() throws IOException {

		TimeValue value = new TimeValue(50000);

		String json = objectMapper.writeValueAsString(value);
		System.err.println(json);
		TimeValue read = objectMapper.readValue(json, TimeValue.class);
		assertEquals(value, read);
	}

}