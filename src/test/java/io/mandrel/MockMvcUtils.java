package io.mandrel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.CollectionUtils;

public abstract class MockMvcUtils {

	public static void print(ResultActions result) throws IOException, UnsupportedEncodingException {
		System.err.println(IOUtils.toString(result.andReturn().getRequest().getInputStream()));
		CollectionUtils.toIterator(result.andReturn().getRequest().getHeaderNames()).forEachRemaining(
				h -> System.err.println(h + ":" + result.andReturn().getRequest().getHeader(h)));

		System.err.println(result.andReturn().getResponse().getContentAsString());
		result.andReturn().getResponse().getHeaderNames().forEach(h -> System.err.println(h + ":" + result.andReturn().getResponse().getHeader(h)));
	}
}
