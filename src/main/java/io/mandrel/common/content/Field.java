package io.mandrel.common.content;

import lombok.Data;

@Data
public class Field {
	private String name;
	private FieldExtractor extractor;
	private FieldFormatter formatter;
}
