package io.mandrel.common.data;

import java.io.Serializable;

import lombok.Data;

@Data
public class Header implements Serializable {
	private static final long serialVersionUID = -600885429254608967L;

	private String name;
	private String value;
}
