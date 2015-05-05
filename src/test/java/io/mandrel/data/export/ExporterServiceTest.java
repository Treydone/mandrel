package io.mandrel.data.export;

import io.mandrel.common.data.Spider;
import io.mandrel.data.spider.SpiderService;
import io.mandrel.gateway.WebPageStore;
import io.mandrel.http.WebPage;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExporterServiceTest {

	@Mock
	private SpiderService spiderService;

	@Mock
	private HttpServletResponse response;

	@Mock
	private WebPageStore store;

	private ExporterService service;

	@Before
	public void before() {
		service = new ExporterService(spiderService);
	}

	@Test
	public void raw_export() {

		// Arrange
		List<WebPage> results = new ArrayList<>();

		RawExporter exporter = new RawExporter() {
			@Override
			public void export(Stream<WebPage> documents, Writer writer) throws IOException {
				documents.forEach(results::add);
			}

			@Override
			public String contentType() {
				return null;
			}
		};

		Spider spider = new Spider();
		spider.getStores().setPageStore(store);

		Mockito.when(spiderService.get(0)).thenReturn(Optional.of(spider));
		Mockito.when(store.all(0)).thenReturn(Stream.of(new WebPage(), new WebPage()));

		// Actions
		service.export(0L, exporter, response);

		// Asserts
		Assertions.assertThat(results).hasSize(2);
	}
}
