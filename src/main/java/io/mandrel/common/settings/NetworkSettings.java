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
package io.mandrel.common.settings;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "network")
@Data
public class NetworkSettings {

	private boolean reuseAddress = true;

	@Data
	public static class Group {
		private String name = "Mandrel";
		private String password = "M@ndR3L";
	}

	@Data
	public static class Discovery {
		private Unicast unicast;
		private Multicast multicast = new Multicast();
		private Ec2 ec2;

		@Data
		public static class Unicast {
			private boolean enabled = false;
			private List<String> members = new ArrayList<>();
			private String requiredMember;
			private int connectionTimeout = 5;
		}

		@Data
		public static class Multicast {
			private boolean enabled = true;
			private String group = "224.2.2.4";
			private int port = 54328;
			private String address = null;
			private int timeToLive = 3;
			private int timeout = 3;
		}

		@Data
		public static class Ec2 {
			private boolean enabled = true;
			private String accessKey;
			private String secretKey;
		}
	}

	@Data
	public static class Tcp {
		private boolean noDelay = true;
		private boolean keepAlive = true;
		private Integer sendBufferSize = null;
		private Integer receiveBufferSize = null;
		private int connectTimeout = 15;
	}

	private List<String> interfaces;
	private Group group = new Group();
	private Tcp tcp;
	private Discovery discovery = new Discovery();

}
