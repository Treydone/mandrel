package io.mandrel.data.spider;

import java.io.Serializable;

import lombok.Data;

@Data
public class Link implements Serializable {

	private static final long serialVersionUID = -7302907794790398632L;

	private String uri;
	private String text;
	private String title;
	private String rel;
}
