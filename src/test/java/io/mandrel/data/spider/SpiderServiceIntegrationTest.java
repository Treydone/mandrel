package io.mandrel.data.spider;

import io.mandrel.common.data.Constants;
import io.mandrel.common.data.Spider;
import io.mandrel.common.settings.ClientSettings;
import io.mandrel.data.content.selector.SelectorService;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.gateway.PageMetadataStore;
import io.mandrel.gateway.WebPageStore;
import io.mandrel.http.Requester;
import io.mandrel.http.WebPage;
import io.mandrel.script.ScriptingService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

@RunWith(MockitoJUnitRunner.class)
public class SpiderServiceIntegrationTest {

	@Mock
	private SpiderRepository spiderRepository;

	@Mock
	private WebPageStore pageStore;

	@Mock
	private PageMetadataStore metadataStore;

	@Test
	public void client() throws IOException {

		SpiderService spiderService = new SpiderService(spiderRepository, null, new ExtractorService(new ScriptingService(), new SelectorService()),
				new Requester(new ClientSettings()));

		Spider spider = new Spider();
		spider.setName("wikipedia");

		spider.getStores().setPageMetadataStore(metadataStore);
		spider.getStores().setPageStore(pageStore);

		spiderService.validate(spider);

		InputStream body = new ClassPathResource("/data/wikipedia.html").getInputStream();
		WebPage webPage = new WebPage(new URL("http://fr.wikipedia.org/wiki/Wikip%C3%A9dia"), 200, "", null, null, body);
		Analysis report = spiderService.buildReport(spider, webPage);

		System.err.println(report.getOutlinks().get(Constants._DEFAULT_OUTLINKS_EXTRATOR));

	}
}