package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML tweaks for performance (from
 * http://scn.sap.com/community/java/blog/2009/12
 * /04/performance-improvements-in-nw-java-applications-with-xml-processing) <br>
 * - Make XML Factories Static <br>
 * - XPath Evaluations
 *
 */
public class XpathSelector extends BodySelector {

	private static final String DTM_MANAGER_PROP_NAME = "com.sun.org.apache.xml.internal.dtm.DTMManager";
	private static final String DTM_MANAGER_CLASS_NAME = "com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault";

	private final static DocumentBuilderFactory factory;
	static {
		System.setProperty(DTM_MANAGER_PROP_NAME, DTM_MANAGER_CLASS_NAME);
		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(true);
	}

	// private final HtmlCleaner cleaner = new HtmlCleaner();

	@Override
	public String getName() {
		return "xpath";
	}

	@Override
	public Instance init(WebPage webPage, InputStream data) {

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		XPath xPath = XPathFactory.newInstance().newXPath();
		Document document = null;
		try {
			document = builder.parse(data);
			// TagNode node = cleaner.clean(data);
			// document = new DomSerializer(new CleanerProperties())
			// .createDOM(node);
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
		public List<String> select(String value) {
			NodeList nodes;
			try {
				nodes = (NodeList) xPath.evaluate(value, document,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				throw new RuntimeException(e);
			}

			List<String> results = new ArrayList<>();
			for (int i = 0; i < nodes.getLength(); i++) {
				results.add(serializeNode(nodes.item(i)));
			}
			return results;
		}

		// TODO this is shit
		private String serializeNode(Node node) {
			String s = "";
			if (node.getNodeName().equals("#text"))
				return node.getTextContent();
			s += "<" + node.getNodeName();
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					s += " " + attributes.item(i).getNodeName() + "=\""
							+ attributes.item(i).getNodeValue() + "\"";
				}
			}
			NodeList childs = node.getChildNodes();
			if (childs == null || childs.getLength() == 0) {
				s += "/>";
				return s;
			}
			s += ">";
			for (int i = 0; i < childs.getLength(); i++)
				s += serializeNode(childs.item(i));
			s += "</" + node.getNodeName() + ">";
			return s;
		}
	}
}
