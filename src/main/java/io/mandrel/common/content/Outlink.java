package io.mandrel.common.content;

import lombok.Data;

@Data
public class Outlink {
	private FieldExtractor extractor;
	private FieldFormatter formatter;

	public Outlink() {
		extractor = new FieldExtractor();
		extractor.setSource(Source.BODY);
		extractor.setType("css");
		extractor.setValue("a href");
	}
}
