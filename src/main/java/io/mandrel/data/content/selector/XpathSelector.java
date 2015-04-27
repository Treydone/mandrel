package io.mandrel.data.content.selector;

import io.mandrel.http.WebPage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import us.codecraft.xsoup.Xsoup;
import us.codecraft.xsoup.xevaluator.DefaultXElements;
import us.codecraft.xsoup.xevaluator.XElement;
import us.codecraft.xsoup.xevaluator.XPathEvaluator;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class XpathSelector extends BodySelector<XElement> {

	private final static LoadingCache<String, XPathEvaluator> evaluations = CacheBuilder.newBuilder().maximumSize(10000)
			.build(new CacheLoader<String, XPathEvaluator>() {
				@Override
				public XPathEvaluator load(String expression) throws Exception {
					return Xsoup.compile(expression);
				}
			});

	@Override
	public String getName() {
		return "xpath";
	}

	@Override
	public Instance<XElement> init(WebPage webPage, InputStream data, boolean isSegment) {
		Element element;
		try {
			if (!isSegment) {
				element = Jsoup.parse(data, "UTF-8", webPage.getUrl().toString());
			} else {
				element = Jsoup.parseBodyFragment(IOUtils.toString(data, "UTF-8"), webPage.getUrl().toString()).body();
			}
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		return new XpathSelectorInstance(element);
	}

	public class XpathSelectorInstance implements Instance<XElement> {

		private final Element element;

		public XpathSelectorInstance(Element element) {
			super();
			this.element = element;
		}

		@Override
		public <U> List<U> select(String value, DataConverter<XElement, U> converter) {
			try {
				return ((DefaultXElements) evaluations.get(value).evaluate(element)).stream().map(el -> converter.convert(el)).collect(Collectors.toList());
			} catch (ExecutionException e) {
				throw Throwables.propagate(e);
			}
		}
	}
}
