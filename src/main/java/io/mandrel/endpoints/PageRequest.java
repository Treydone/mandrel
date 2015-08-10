package io.mandrel.endpoints;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PageRequest {

	private int draw;
	private int start;
	private int length;
}
