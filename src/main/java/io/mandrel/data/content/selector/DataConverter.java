package io.mandrel.data.content.selector;

import us.codecraft.xsoup.xevaluator.XElement;

public interface DataConverter<T, U> {

	U convert(T t);

	DataConverter<String, String> DEFAULT = new DataConverter<String, String>() {
		public String convert(String input) {
			return input.trim();
		}
	};

	DataConverter<XElement, String> BODY = new DataConverter<XElement, String>() {
		public String convert(XElement xElement) {
			return xElement.get().trim();
		}
	};
}
