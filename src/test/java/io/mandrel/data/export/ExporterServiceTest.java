package io.mandrel.data.export;

import io.mandrel.common.data.Spider;
import io.mandrel.data.spider.SpiderService;
import io.mandrel.gateway.WebPageStore;
import io.mandrel.gateway.WebPageStore.Callback;
import io.mandrel.http.WebPage;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

	@Captor
	private ArgumentCaptor<Callback> captor;

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
			public String contentType() {
				return null;
			}

			public void export(Collection<WebPage> documents) {
				results.addAll(documents);
			}

			public void init(Writer writer) {
			}

			public void close() {
			}
		};

		Spider spider = new Spider();
		spider.getStores().setPageStore(store);

		Mockito.when(spiderService.get(0)).thenReturn(Optional.of(spider));
		// Mockito.when(store.byPages(0L, 1000, captor.capture()));

		// Mockito.when(store.all(0)).thenReturn(Stream.of(new WebPage(), new
		// WebPage()));

		// Actions
		service.export(0L, exporter, response);

		// Asserts
		Assertions.assertThat(results).hasSize(2);
	}
}
