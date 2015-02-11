package io.mandrel.common.content;

import lombok.Data;

@Data
public class Outlink {
	private Extractor extractor;
	private Formatter formatter;

	public Outlink() {
		extractor = new Extractor();
		extractor.setSource(SourceType.BODY);
		extractor.setType("css");
		extractor.setValue("a href");
	}
}
