package io.mandrel.service.spider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class Spider {
	private int requestTimeOut;
	private Map<String, Collection<String>> headers;
	private Map<String, List<String>> params;

}
