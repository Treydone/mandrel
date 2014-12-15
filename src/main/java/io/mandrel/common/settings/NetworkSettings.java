package io.mandrel.common.settings;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "network")
@Data
public class NetworkSettings {

	@Data
	public static class Group {
		private String name;
		private String password;
	}

	@Data
	public static class Discovery {
		private Unicast unicast;
		private Multicast multicast;
		private Ec2 ec2;

		@Data
		public static class Unicast {
			private boolean enabled = true;
		}

		@Data
		public static class Multicast {
			private boolean enabled = true;
			private String group = "224.2.2.4";
			private int port = 54328;
			private String address = null;
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
		private boolean reuseAddress = true;
		private Integer sendBufferSize = null;
		private Integer receiveBufferSize = null;
		private int connectTimeout = 15;
	}

	private List<String> interfaces;
	private Group group;
	private Tcp tcp;
	private Discovery discovery;

}
