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

import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.metadata.FetchMetadata;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import us.codecraft.xsoup.xevaluator.XElement;

public class XpathSelectorTest {

	@Test
	public void test() throws MalformedURLException, URISyntaxException {

		XpathSelector selector = new XpathSelector();

		byte[] data = "<a href='/test'>épatant</a>".getBytes();
		FetchMetadata webPage = new FetchMetadata().setUri(new URI("http://localhost"));
		Instance<XElement> instance = selector.init(webPage, data, false);

		List<String> results = instance.select("//a/@href", DataConverter.BODY);
		Assertions.assertThat(results).containsExactly("/test");

		results = instance.select("//a/text()", DataConverter.BODY);
		Assertions.assertThat(results).containsExactly("épatant");
	}
}
