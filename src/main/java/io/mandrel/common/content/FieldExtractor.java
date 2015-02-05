package io.mandrel.common.content;

import lombok.Data;

@Data
public class FieldExtractor {
	private String type;
	private String value;
	private Source source;

}
