/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.task;

import io.mandrel.common.data.Spider;

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

	public void executeOnLocalMember(String suffix, Runnable callable) {
		Member member = hazelcastInstance.getCluster().getLocalMember();
		hazelcastInstance.getExecutorService(EXECUTOR_PREFIX + suffix).executeOnMember(callable, member);
	}

	public <T> Entry<Member, Future<T>> executeOnLocalMember(String suffix, Callable<T> callable) {
		Member member = hazelcastInstance.getCluster().getLocalMember();
		Future<T> result = hazelcastInstance.getExecutorService(EXECUTOR_PREFIX + suffix).submitToMember(callable, member);
		return new DefaultMapEntry(member, result);
	}

	public void prepareSimpleExecutor(String suffix) {
		hazelcastInstance.getConfig().getExecutorConfig(EXECUTOR_PREFIX + suffix).setPoolSize(1).setStatisticsEnabled(true).setQueueCapacity(1);
	}

	public void shutdownAllExecutorService(Spider spider) {
		if (spider.getSources() != null) {
			spider.getSources().stream().forEach(source -> {
				String sourceExecServiceName = "executor-" + spider.getId() + "-source-" + source.getName();
				shutdownDistributedExecutorService(sourceExecServiceName);
			});
		}

		String taskExecServiceName = "executor-" + spider.getId();
		shutdownDistributedExecutorService(taskExecServiceName);
	}

	/**
	 * Shutdown a distributed executor service.
	 * 
	 * @param taskExecServiceName
	 */
	public void shutdownDistributedExecutorService(String taskExecServiceName) {
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
