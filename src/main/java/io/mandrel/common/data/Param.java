package io.mandrel.common.data;

import java.io.Serializable;

import lombok.Data;

@Data
public class Param implements Serializable {

	private static final long serialVersionUID = 7540539281958319520L;

	private String name;
	private String value;
}
