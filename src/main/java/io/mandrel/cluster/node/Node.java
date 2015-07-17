package io.mandrel.cluster.node;

import io.mandrel.monitor.Infos;

import java.io.Serializable;

import lombok.Data;

@Data
public class Node implements Serializable {
	private static final long serialVersionUID = 9044434196832084086L;

	private String uuid;
	private Infos infos;
}
