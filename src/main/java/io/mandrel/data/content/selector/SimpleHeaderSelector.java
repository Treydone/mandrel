package io.mandrel.data.content.selector;

import io.mandrel.http.WebPage;

import java.util.List;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;

public class SimpleHeaderSelector extends HeaderSelector {

	public Instance init(WebPage webpage, FluentCaseInsensitiveStringsMap headers) {
		return new Instance() {

			@Override
			public List<String> select(String value) {
				return headers.get(value);
			}
		};
	}

	@Override
	public String getName() {
		return "header";
	}
}
