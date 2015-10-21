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
import io.mandrel.common.data.Spider;
import io.mandrel.document.DocumentStores;
import io.mandrel.frontier.FrontierClient;
import io.mandrel.metadata.MetadataStores;
import io.mandrel.worker.WorkerClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

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

		private final FrontierClient frontierClient;
		private final Spider spider;

		@Override
		public void apply() {
			frontierClient.create(spider);
		}

		@Override
		public void undo() {
			frontierClient.kill(spider.getId());
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class StartFrontier implements Command, Rollbackable {

		private final FrontierClient frontierClient;
		private final Spider spider;

		@Override
		public void apply() {
			frontierClient.start(spider.getId());
		}

		@Override
		public void undo() {
			frontierClient.kill(spider.getId());
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class KillFrontier implements Command {

		private final FrontierClient frontierClient;
		private final Spider spider;

		@Override
		public void apply() {
			frontierClient.kill(spider.getId());
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class PrepareWorker implements Command, Rollbackable {

		private final WorkerClient workerClient;
		private final Spider spider;

		@Override
		public void apply() {
			workerClient.create(spider);
		}

		@Override
		public void undo() {
			workerClient.kill(spider.getId());
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class StartWorker implements Command, Rollbackable {

		private final WorkerClient workerClient;
		private final Spider spider;

		@Override
		public void apply() {
			workerClient.start(spider.getId());
		}

		@Override
		public void undo() {
			workerClient.kill(spider.getId());
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class KillWorker implements Command {

		private final WorkerClient workerClient;
		private final Spider spider;

		@Override
		public void apply() {
			workerClient.kill(spider.getId());
		}
	}

	//
	// FRONTIER
	//
	public static CreateFrontier prepareFrontier(FrontierClient frontierClient, Spider spider) {
		return new CreateFrontier(frontierClient, spider);
	}

	public static StartFrontier startFrontier(FrontierClient frontierClient, Spider spider) {
		return new StartFrontier(frontierClient, spider);
	}

	public static KillFrontier killFrontier(FrontierClient frontierClient, Spider spider) {
		return new KillFrontier(frontierClient, spider);
	}

	//
	// WORKER
	//
	public static PrepareWorker prepareWorker(WorkerClient workerClient, Spider spider) {
		return new PrepareWorker(workerClient, spider);
	}

	public static StartWorker startWorker(WorkerClient workerClient, Spider spider) {
		return new StartWorker(workerClient, spider);
	}

	public static KillWorker killWorker(WorkerClient workerClient, Spider spider) {
		return new KillWorker(workerClient, spider);
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
