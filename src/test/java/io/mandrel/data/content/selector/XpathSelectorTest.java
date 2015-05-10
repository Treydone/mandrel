package io.mandrel.data.content.selector;

import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.http.WebPage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import us.codecraft.xsoup.xevaluator.XElement;

public class XpathSelectorTest {

	@Test
	public void test() throws MalformedURLException {

		XpathSelector selector = new XpathSelector();

		byte[] data = "<a href='/test'>épatant</a>".getBytes();
		WebPage webPage = new WebPage().setUrl(new URL("http://localhost"));
		Instance<XElement> instance = selector.init(webPage, data, false);

		List<String> results = instance.select("//a/@href", DataConverter.BODY);
		Assertions.assertThat(results).containsExactly("/test");

		results = instance.select("//a/text()", DataConverter.BODY);
		Assertions.assertThat(results).containsExactly("épatant");
	}
}
