package io.mandrel.client.content.selector;

import io.mandrel.common.WebPage;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XpathSelector implements WebPageSelector {

	private final HtmlCleaner cleaner = new HtmlCleaner();

	@Override
	public String getName() {
		return "xpath";
	}

	public Instance init(WebPage webPage) {
		XPath xPath = XPathFactory.newInstance().newXPath();
		Document document = null;
		try {
			TagNode node = cleaner.clean(webPage.getDataStream());
			document = new DomSerializer(new CleanerProperties())
					.createDOM(node);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new XpathSelectorInstance(xPath, document);

	}

	public class XpathSelectorInstance implements Instance {

		private final XPath xPath;

		private final Document document;

		public XpathSelectorInstance(XPath xPath, Document document) {
			super();
			this.xPath = xPath;
			this.document = document;
		}

		@Override
		public List<Object> select(byte[] value) {
			NodeList nodes;
			try {
				nodes = (NodeList) xPath.evaluate(new String(value), document,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				throw new RuntimeException(e);
			}

			List<Object> results = new ArrayList<>();
			for (int i = 0; i < nodes.getLength(); i++) {
				results.add(nodes.item(i).getNodeValue());
			}
			return results;
		}

	}
}
