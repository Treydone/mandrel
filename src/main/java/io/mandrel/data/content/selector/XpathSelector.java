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
package io.mandrel.data.content.selector;

import io.mandrel.requests.Metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import us.codecraft.xsoup.Xsoup;
import us.codecraft.xsoup.xevaluator.DefaultXElements;
import us.codecraft.xsoup.xevaluator.XElement;
import us.codecraft.xsoup.xevaluator.XPathEvaluator;

import com.google.common.base.Charsets;
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
	public Instance<XElement> init(Metadata data, byte[] bytes, boolean isSegment) {
		Element element;
		try {
			if (!isSegment) {
				element = Jsoup.parse(new ByteArrayInputStream(bytes), Charsets.UTF_8.name(), data.getUri().toString());
			} else {
				element = Jsoup.parseBodyFragment(new String(bytes, Charsets.UTF_8), data.getUri().toString()).body();
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
