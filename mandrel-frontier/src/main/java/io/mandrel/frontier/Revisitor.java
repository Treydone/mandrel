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
package io.mandrel.frontier;

import io.mandrel.metadata.MetadataStore;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Revisitor implements Runnable {

	private final AtomicBoolean run = new AtomicBoolean(false);

	private final Frontier frontier;
	private final MetadataStore metadatastore;

	public void start() {
		run.set(true);
	}

	public void pause() {
		run.set(false);
	}

	public boolean isRunning() {
		return run.get();
	}

	@Override
	public void run() {

		while (true) {

			if (!run.get()) {
				log.trace("Waiting...");
				try {
					TimeUnit.MILLISECONDS.sleep(2000);
				} catch (InterruptedException e) {
					// Don't care
					log.trace("", e);
				}
				continue;
			}

			metadatastore.byPages(1000, entries -> {
				try {
					if (entries != null) {
						entries.forEach(entry -> {
							if (frontier.revisit().isScheduledForRevisit(entry)) {
								frontier.schedule(entry.getUri());
							}
						});
					}
				} catch (Exception e) {
					log.debug("Uhhh...", e);
					return false;
				}
				return CollectionUtils.isNotEmpty(entries);
			});
		}
	}
}
