package io.mandrel.service.node;

import io.mandrel.service.task.TaskService;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.PathParam;

import org.springframework.stereotype.Component;

import com.hazelcast.core.Member;

@Component
public class NodeService {

	private final TaskService taskService;

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
	public NodeService(TaskService taskService) {
		this.taskService = taskService;
	}

	public List<Node> all() {
		return taskService.executeOnAllMembers(new NodeTask()).entrySet().stream().map(mapper).collect(Collectors.toList());
	}

	public Node id(@PathParam("id") String id) {
		Entry<Member, Future<Map<String, Object>>> result = taskService.executeOnMember(new NodeTask(), id);
		return mapper.apply(result);
	}

	public Node dhis() {
		Entry<Member, Future<Map<String, Object>>> result = taskService.executeOnLocalMember(new NodeTask());
		return mapper.apply(result);
	}
}
