package io.mandrel.service.task;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.keyvalue.DefaultMapEntry;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

@Component
@Slf4j
public class TaskService {

	private static final String EXECUTOR_PREFIX = "executor-";

	private final HazelcastInstance hazelcastInstance;

	@Inject
	public TaskService(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	public void executeOnAllMembers(Runnable task) {
		executorService().executeOnAllMembers(task);
	}

	public <T> Map<Member, Future<T>> executeOnAllMembers(Callable<T> callable) {
		return executorService().submitToAllMembers(callable);
	}

	public void executeOnAllMembers(String suffix, Runnable task) {
		hazelcastInstance.getExecutorService(EXECUTOR_PREFIX + suffix).executeOnAllMembers(task);
	}

	public void executeOnRandomMember(String suffix, Runnable task) {
		hazelcastInstance.getExecutorService(EXECUTOR_PREFIX + suffix).execute(task, ms -> true);
	}

	public <T> Entry<Member, Future<T>> executeOnMember(Callable<T> callable, String id) {
		Member member = hazelcastInstance.getCluster().getMembers().stream().filter(m -> m.getUuid().equalsIgnoreCase(id)).findFirst().get();
		Future<T> result = executorService().submitToMember(callable, member);
		return new DefaultMapEntry(member, result);
	}

	public <T> Entry<Member, Future<T>> executeOnLocalMember(Callable<T> callable) {
		Member member = hazelcastInstance.getCluster().getLocalMember();
		Future<T> result = executorService().submitToMember(callable, member);
		return new DefaultMapEntry(member, result);
	}

	public void prepareSimpleExecutor(String suffix) {
		hazelcastInstance.getConfig().getExecutorConfig(EXECUTOR_PREFIX + suffix).setPoolSize(1).setStatisticsEnabled(true).setQueueCapacity(1);
	}

	public void shutdownExecutorService(String taskExecServiceName) {
		IExecutorService pool = hazelcastInstance.getExecutorService(taskExecServiceName);
		pool.shutdown();
		try {
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					log.debug("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
		pool.destroy();
	}

	private IExecutorService executorService() {
		return hazelcastInstance.getExecutorService("_shared");
	}

}
