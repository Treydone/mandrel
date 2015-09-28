/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.apache.tika.sax.xpath.Matcher;
import org.apache.tika.sax.xpath.MatchingContentHandler;
import org.apache.tika.sax.xpath.XPathParser;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

public class SparkTest {

	@Test
	public void test1() {

		XPathParser xhtmlParser = new XPathParser("xhtml", XHTMLContentHandler.XHTML);
		Matcher xpathContentMatcher = xhtmlParser.parse("/xhtml:html/xhtml:head/xhtml:title/text()");
		HtmlParser parser = new HtmlParser();

		MatchingContentHandler contentHandler = new MatchingContentHandler(new ToXMLContentHandler(), xpathContentMatcher);
		LinkContentHandler linkHandler = new LinkContentHandler();
		TeeContentHandler teeHandler = new TeeContentHandler(linkHandler, contentHandler);

		Metadata metadata = new Metadata();
		ParseContext parseContext = new ParseContext();
		try {
			parser.parse(new ClassPathResource("/data/wikipedia.html").getInputStream(), teeHandler, metadata, parseContext);
			System.err.println(linkHandler.getLinks().stream().map(l -> l.getUri()).collect(Collectors.toList()));
			System.err.println(contentHandler.toString());
		} catch (IllegalStateException | SAXException | TikaException | IOException e) {
			e.printStackTrace();
		}
	}

	private List<Object> getInBatch(Iterator<String> bunchOfUrls) {

		XPathParser xhtmlParser = new XPathParser();
		Matcher xpathContentMatcher = xhtmlParser.parse("/html/body/div/descendant::node()");

		HtmlParser parser = new HtmlParser();

		try (CloseableHttpClient client = HttpClients.createMinimal(ConnectionManager.getHttpClientConnectionManager())) {
			HttpUriRequest request = new HttpGet("");

			try (CloseableHttpResponse response = client.execute(request)) {
				MatchingContentHandler contentHandler = new MatchingContentHandler(new ToXMLContentHandler(), xpathContentMatcher);
				LinkContentHandler linkHandler = new LinkContentHandler();
				TeeContentHandler teeHandler = new TeeContentHandler(linkHandler, contentHandler);

				Metadata metadata = new Metadata();
				ParseContext parseContext = new ParseContext();
				try {
					parser.parse(response.getEntity().getContent(), teeHandler, metadata, parseContext);
					linkHandler.getLinks();
					contentHandler.toString();
				} catch (IllegalStateException | SAXException | TikaException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Object get(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	static class ConnectionManager {
		private final static HttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();

		public static HttpClientConnectionManager getHttpClientConnectionManager() {
			return httpClientConnectionManager;
		}

	}
}
