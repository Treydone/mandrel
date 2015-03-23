package io.mandrel.discovery;

import io.mandrel.node.Node;

import java.util.List;

public interface DiscoveryService {

	List<Node> all();

	Node id(String id);

	Node dhis();

}
