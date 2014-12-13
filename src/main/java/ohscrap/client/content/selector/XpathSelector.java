package ohscrap.client.content.selector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

public class XpathSelector implements WebPageSelector {

	
	@Override
	public String getName() {
		return "xpath";
	}

	@Override
	public Object select(byte[] value) {
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		return null;
	}
}
