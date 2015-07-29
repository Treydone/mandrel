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

import io.mandrel.data.source.Source;
import io.mandrel.messaging.UrlsQueueService;

import java.io.Serializable;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
@Setter
@Slf4j
public class SourceTask implements Runnable, HazelcastInstanceAware, Serializable {

	private static final long serialVersionUID = -6204571043673228240L;

	private long spiderId;
	private Source source;

	@Autowired
	private transient UrlsQueueService urlsQueueService;

	@Autowired
	@Getter(value = AccessLevel.NONE)
	private transient HazelcastInstance hazelcastInstance;

	public SourceTask(long spiderId, Source source) {
		this.spiderId = spiderId;
		this.source = source;
	}

	@Override
	public void run() {
		// TODO

		try {
			Map<String, Object> properties = null;

			source.setInstance(hazelcastInstance);
			source.init(properties);

			source.register(lst -> {
				urlsQueueService.add(spiderId, Sets.newHashSet(lst));
			});
		} catch (Exception e) {
			log.warn("Can not start source", e);
		}
	}
}