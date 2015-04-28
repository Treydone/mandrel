package io.mandrel.data.spider;

import lombok.Data;

@Data
public class Link {

	private String uri;
	private String text;
	private String title;
	private String rel;
}
