package io.mandrel.data.content;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Formatter implements Serializable {

	private static final long serialVersionUID = -8298561819249613733L;

	@JsonProperty("type")
	private String type;

	@JsonProperty("value")
	private byte[] value;

}
