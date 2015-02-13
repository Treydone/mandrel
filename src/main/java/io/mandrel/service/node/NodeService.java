package io.mandrel.service.node;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.PathParam;

import org.apache.commons.collections.keyvalue.DefaultMapEntry;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

@Component
public class NodeService {

	private final HazelcastInstance hazelcastInstance;

	private final Function<? super Entry<Member, Future<Map<String, Object>>>, ? extends Node> mapper = kv -> {
		Node node = new Node();
		node.setUuid(kv.getKey().getUuid());
		try {
			node.setInfos(kv.getValue().get());
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
		return node;
	};

	@Inject
	public NodeService(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	public List<Node> all() {
		return executorService().submitToAllMembers(new NodeTask()).entrySet().stream().map(mapper).collect(Collectors.toList());
	}

	public Node id(@PathParam("id") String id) {
		Member member = hazelcastInstance.getCluster().getMembers().stream().filter(m -> m.getUuid().equalsIgnoreCase(id)).findFirst().get();
		Future<Map<String, Object>> result = executorService().submitToMember(new NodeTask(), member);
		return mapper.apply(new DefaultMapEntry(member, result));
	}

	public Node dhis() {
		Member member = hazelcastInstance.getCluster().getLocalMember();
		Future<Map<String, Object>> result = executorService().submitToMember(new NodeTask(), member);
		return mapper.apply(new DefaultMapEntry(member, result));
	}

	public void executeOnAllMembers(Runnable task) {
		executorService().executeOnAllMembers(task);
	}

	private IExecutorService executorService() {
		return hazelcastInstance.getExecutorService("_shared");
	}
}
