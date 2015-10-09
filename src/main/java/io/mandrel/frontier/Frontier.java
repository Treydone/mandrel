package io.mandrel.frontier;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public abstract class Frontier implements Serializable {

	private static final long serialVersionUID = -7584623252876875042L;

	@JsonProperty("politeness")
	private Politeness politeness = new Politeness();
}
