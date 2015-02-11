package io.mandrel.common.content;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Formatter {
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("value")
	private byte[] value;

}
