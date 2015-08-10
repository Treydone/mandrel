package io.mandrel.common.data;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Param implements Serializable {

	private static final long serialVersionUID = 7540539281958319520L;

	private String name;
	private String value;
}
