package io.mandrel.data.content;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class OutlinkExtractor extends NamedDataExtractorFormatter {

	private static final long serialVersionUID = -4094495903167374714L;

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
