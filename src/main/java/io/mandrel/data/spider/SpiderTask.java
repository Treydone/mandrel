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
package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.frontier.FrontierService;
import io.mandrel.requests.Requester;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
@Setter
@Slf4j
public class SpiderTask implements Runnable, HazelcastInstanceAware, Serializable {

	private static final long serialVersionUID = -6204571043673228240L;

	private Spider spider;

	@Autowired
	private transient FrontierService urlsQueueService;

	@Autowired
	private transient SpiderService spiderService;

	@Autowired
	@Getter(value = AccessLevel.NONE)
	private transient HazelcastInstance hazelcastInstance;

	public SpiderTask(Spider spider) {
		this.spider = spider;
	}

	@Override
	public void run() {

		try {
			spiderService.injectAndInit(spider);

			for (Requester requester : spider.getClient().getRequesters()) {
				// Prepare client
				if (requester.getStrategy().getNameResolver() != null) {
					requester.getStrategy().getNameResolver().init();
				}
				if (requester.getStrategy().getProxyServersSource() != null) {
					requester.getStrategy().getProxyServersSource().init();
				}
				requester.init();
			}

			if (spider.getExtractors().getPages() != null) {
				spider.getExtractors().getPages().stream().forEach(ex -> ex.getDocumentStore().init(ex));
			}

			// Block until the end
			urlsQueueService.registrer(spider);

			// End the spider on all members
			spiderService.end(spider.getId());
		} catch (Exception e) {
			log.warn("Can not start spider", e);
		}
	}
}