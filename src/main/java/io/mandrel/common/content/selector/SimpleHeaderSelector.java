package io.mandrel.common.content.selector;

import io.mandrel.common.WebPage;

import java.util.List;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;

public class SimpleHeaderSelector extends HeaderSelector {

	public Instance init(WebPage webpage, FluentCaseInsensitiveStringsMap headers) {
		return new Instance() {

			@Override
			public List<String> select(String value) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public String getName() {
		return "header";
	}
}
