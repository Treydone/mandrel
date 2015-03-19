package io.mandrel.node;

import java.util.Map;

import lombok.Data;

@Data
public class Node {
	private String uuid;
	private Map<String, Object> infos;
}
