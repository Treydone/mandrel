package io.mandrel.node;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

@Data
public class Node implements Serializable {
	private static final long serialVersionUID = 9044434196832084086L;

	private String uuid;
	private Map<String, Object> infos;
}
