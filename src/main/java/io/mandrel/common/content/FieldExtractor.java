package io.mandrel.common.content;

import lombok.Data;

@Data
public class FieldExtractor {
	private String type;
	private byte[] value;
	private Source source;

}
