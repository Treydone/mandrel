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
package io.mandrel.timeline;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

import com.facebook.swift.codec.ThriftStruct;

@Data
@Accessors(chain = true)
@ThriftStruct
public class Event {

	private LocalDateTime time;
	private String title;
	private String text;

	private EventType type;
	private SpiderInfo spider;
	private NodeInfo node;

	public enum EventType {
		SPIDER, NODE
	}

	public static Event forSpider() {
		return new Event().setTime(LocalDateTime.now()).setType(EventType.SPIDER).setSpider(new SpiderInfo());
	}

	public static Event forNode() {
		return new Event().setTime(LocalDateTime.now()).setType(EventType.NODE).setNode(new NodeInfo());
	}

	@Data
	@Accessors(chain = true)
	@ThriftStruct
	public static class SpiderInfo {

		private SpiderEventType type;
		private long spiderId;
		private String spiderName;

		public enum SpiderEventType {
			SPIDER_CREATED, SPIDER_STARTED, SPIDER_PAUSED, SPIDER_UPDATED, SPIDER_ENDED, SPIDER_KILLED, SPIDER_DELETED
		}

	}

	@Data
	@Accessors(chain = true)
	@ThriftStruct
	public static class NodeInfo {

		private NodeEventType type;
		private String nodeId;

		public enum NodeEventType {
			NODE_STARTED, NODE_STOPPED
		}
	}
}
