package io.mandrel.cluster.node;

import java.net.URI;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface NodeRepository extends MongoRepository<Node, URI> {

}
