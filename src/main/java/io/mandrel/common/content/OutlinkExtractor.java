package io.mandrel.common.content;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class OutlinkExtractor extends NamedDataExtractorFormatter {

	private String name;
	private Extractor extractor;
	private Formatter formatter;

	public OutlinkExtractor(String name) {
		this.extractor = new Extractor();
		this.extractor.setSource(SourceType.BODY);
		this.extractor.setType("css");
		this.extractor.setValue("a href");
		this.name = name;
	}

	public OutlinkExtractor() {
	}
}
