package io.mandrel.common.content;

import lombok.Data;

@Data
public class Extractor {
	private String type;
	private String value;
	private SourceType source;

}
