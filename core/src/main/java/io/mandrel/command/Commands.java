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
package io.mandrel.command;

import io.mandrel.blob.BlobStores;
import io.mandrel.cluster.discovery.ServiceIds;
import io.mandrel.common.client.FrontierClient;
import io.mandrel.common.client.WorkerClient;
import io.mandrel.common.data.Spider;
import io.mandrel.document.DocumentStores;
import io.mandrel.metadata.MetadataStores;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import org.springframework.cloud.client.discovery.DiscoveryClient;

public class Commands {

	@FunctionalInterface
	public interface Command {
		public void apply();
	}

	@FunctionalInterface
	public interface Rollbackable {
		public void undo();
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class CommandGroup implements Command, Rollbackable {

		private final List<Command> commands;

		private final List<Command> doneCommands = new ArrayList<>();

		private boolean forceUndo = true;

		@Override
		public void apply() {
			commands.forEach(command -> {
				command.apply();
				doneCommands.add(command);
			});
		}

		@Override
		public void undo() {
			if (doneCommands != null) {
				Collections.reverse(doneCommands);
				doneCommands.stream().filter(c -> c instanceof Rollbackable).map(c -> (Rollbackable) c).forEach(command -> {
					try {
						command.undo();
					} catch (Exception e) {
						if (!forceUndo) {
							throw e;
						}
					}
				});
			}
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class Delete implements Command {

		private final Spider spider;

		@Override
		public void apply() {
			BlobStores.get(spider.getId()).ifPresent(b -> b.deleteAll());
			MetadataStores.get(spider.getId()).deleteAll();
			DocumentStores.get(spider.getId()).ifPresent(d -> d.entrySet().forEach(e -> {
				e.getValue().deleteAll();
			}));

		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class CreateFrontier implements Command, Rollbackable {

		private final DiscoveryClient discoveryClient;
		private final FrontierClient frontierClient;
		private final Spider spider;

		@Override
		public void apply() {
			Runner.runOnAllInstaces(discoveryClient, ServiceIds.FRONTIER, (i) -> frontierClient.create(spider, i.getUri()));
		}

		@Override
		public void undo() {
			Runner.runOnAllInstaces(discoveryClient, ServiceIds.FRONTIER, (i) -> frontierClient.kill(spider.getId(), i.getUri()));
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class StartFrontier implements Command, Rollbackable {

		private final DiscoveryClient discoveryClient;
		private final FrontierClient frontierClient;
		private final Spider spider;

		@Override
		public void apply() {
			Runner.runOnAllInstaces(discoveryClient, ServiceIds.FRONTIER, (i) -> frontierClient.start(spider.getId(), i.getUri()));
		}

		@Override
		public void undo() {
			Runner.runOnAllInstaces(discoveryClient, ServiceIds.FRONTIER, (i) -> frontierClient.kill(spider.getId(), i.getUri()));
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class KillFrontier implements Command {

		private final DiscoveryClient discoveryClient;
		private final FrontierClient frontierClient;
		private final Spider spider;

		@Override
		public void apply() {
			Runner.runOnAllInstaces(discoveryClient, ServiceIds.FRONTIER, (i) -> frontierClient.kill(spider.getId(), i.getUri()));
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class PrepareWorker implements Command, Rollbackable {

		private final DiscoveryClient discoveryClient;
		private final WorkerClient workerClient;
		private final Spider spider;

		@Override
		public void apply() {
			Runner.runOnAllInstaces(discoveryClient, ServiceIds.WORKER, (i) -> workerClient.create(spider, i.getUri()));
		}

		@Override
		public void undo() {
			Runner.runOnAllInstaces(discoveryClient, ServiceIds.WORKER, (i) -> workerClient.kill(spider.getId(), i.getUri()));
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class StartWorker implements Command, Rollbackable {

		private final DiscoveryClient discoveryClient;
		private final WorkerClient workerClient;
		private final Spider spider;

		@Override
		public void apply() {
			Runner.runOnAllInstaces(discoveryClient, ServiceIds.WORKER, (i) -> workerClient.start(spider.getId(), i.getUri()));
		}

		@Override
		public void undo() {
			Runner.runOnAllInstaces(discoveryClient, ServiceIds.WORKER, (i) -> workerClient.kill(spider.getId(), i.getUri()));
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class KillWorker implements Command {

		private final DiscoveryClient discoveryClient;
		private final WorkerClient workerClient;
		private final Spider spider;

		@Override
		public void apply() {
			Runner.runOnAllInstaces(discoveryClient, ServiceIds.WORKER, (i) -> workerClient.kill(spider.getId(), i.getUri()));
		}
	}

	//
	// FRONTIER
	//
	public static CreateFrontier prepareFrontier(DiscoveryClient discoveryClient, FrontierClient frontierClient, Spider spider) {
		return new CreateFrontier(discoveryClient, frontierClient, spider);
	}

	public static StartFrontier startFrontier(DiscoveryClient discoveryClient, FrontierClient frontierClient, Spider spider) {
		return new StartFrontier(discoveryClient, frontierClient, spider);
	}

	public static KillFrontier killFrontier(DiscoveryClient discoveryClient, FrontierClient frontierClient, Spider spider) {
		return new KillFrontier(discoveryClient, frontierClient, spider);
	}

	//
	// WORKER
	//
	public static PrepareWorker prepareWorker(DiscoveryClient discoveryClient, WorkerClient workerClient, Spider spider) {
		return new PrepareWorker(discoveryClient, workerClient, spider);
	}

	public static StartWorker startWorker(DiscoveryClient discoveryClient, WorkerClient workerClient, Spider spider) {
		return new StartWorker(discoveryClient, workerClient, spider);
	}

	public static KillWorker killWorker(DiscoveryClient discoveryClient, WorkerClient workerClient, Spider spider) {
		return new KillWorker(discoveryClient, workerClient, spider);
	}

	//
	// OTHERS
	//
	public static CommandGroup groupOf(Command... commands) {
		return new CommandGroup(Arrays.asList(commands));
	}

	public static Delete delete(Spider spider) {
		return new Delete(spider);
	}

}
