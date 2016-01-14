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
package io.mandrel.monitor;

import io.mandrel.common.unit.ByteSizeValue;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@Accessors(chain = true)
@ThriftStruct
public class Infos implements Serializable {
	private static final long serialVersionUID = 3436344116385574896L;

	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private long pid;
	@Getter(onMethod = @__(@ThriftField(2)))
	@Setter(onMethod = @__(@ThriftField))
	private String fqdn;
	@Getter(onMethod = @__(@ThriftField(3)))
	@Setter(onMethod = @__(@ThriftField))
	private String hostname;
	@Getter(onMethod = @__(@ThriftField(4)))
	@Setter(onMethod = @__(@ThriftField))
	private double uptime;

	@Getter(onMethod = @__(@ThriftField(10)))
	@Setter(onMethod = @__(@ThriftField))
	private Cpu cpu = new Cpu();
	@Getter(onMethod = @__(@ThriftField(11)))
	@Setter(onMethod = @__(@ThriftField))
	private Mem mem = new Mem();
	@Getter(onMethod = @__(@ThriftField(12)))
	@Setter(onMethod = @__(@ThriftField))
	private Swap swap = new Swap();
	@Getter(onMethod = @__(@ThriftField(13)))
	@Setter(onMethod = @__(@ThriftField))
	private List<Interface> interfaces = new ArrayList<>(2);
	@Getter(onMethod = @__(@ThriftField(14)))
	@Setter(onMethod = @__(@ThriftField))
	private Limits limits = new Limits();
	@Getter(onMethod = @__(@ThriftField(15)))
	@Setter(onMethod = @__(@ThriftField))
	private JvmInfo jvmInfo = JvmInfo.jvmInfo();

	@ThriftStruct
	public static class Cpu implements Serializable {
		private static final long serialVersionUID = 8512684069342496081L;

		@Getter(onMethod = @__(@ThriftField(1)))
		@Setter(onMethod = @__(@ThriftField))
		private long sys;
		@Getter(onMethod = @__(@ThriftField(2)))
		@Setter(onMethod = @__(@ThriftField))
		private long total;
		@Getter(onMethod = @__(@ThriftField(3)))
		@Setter(onMethod = @__(@ThriftField))
		private long user;
	}

	@ThriftStruct
	public static class Mem implements Serializable {
		private static final long serialVersionUID = -8797648466105477814L;

		@Getter(onMethod = @__(@ThriftField(1)))
		@Setter(onMethod = @__(@ThriftField))
		private long total;
		@Getter(onMethod = @__(@ThriftField(2)))
		@Setter(onMethod = @__(@ThriftField))
		private long used;
		@Getter(onMethod = @__(@ThriftField(3)))
		@Setter(onMethod = @__(@ThriftField))
		private long free;
	}

	@ThriftStruct
	public static class Swap implements Serializable {
		private static final long serialVersionUID = 1409517375142998802L;

		@Getter(onMethod = @__(@ThriftField(1)))
		@Setter(onMethod = @__(@ThriftField))
		private long total;
		@Getter(onMethod = @__(@ThriftField(2)))
		@Setter(onMethod = @__(@ThriftField))
		private long used;
		@Getter(onMethod = @__(@ThriftField(3)))
		@Setter(onMethod = @__(@ThriftField))
		private long free;
	}

	@ThriftStruct
	public static class Interface implements Serializable {
		private static final long serialVersionUID = 49989037162348232L;

		@Getter(onMethod = @__(@ThriftField(1)))
		@Setter(onMethod = @__(@ThriftField))
		private String name;
		@Getter(onMethod = @__(@ThriftField(2)))
		@Setter(onMethod = @__(@ThriftField))
		private String type;
		@Getter(onMethod = @__(@ThriftField(3)))
		@Setter(onMethod = @__(@ThriftField))
		private String address;
	}

	@ThriftStruct
	public static class Limits implements Serializable {
		private static final long serialVersionUID = 7797483638794413832L;

		@Getter(onMethod = @__(@ThriftField(1)))
		@Setter(onMethod = @__(@ThriftField))
		private Limit openfiles = new Limit();
		@Getter(onMethod = @__(@ThriftField(2)))
		@Setter(onMethod = @__(@ThriftField))
		private Limit cpu = new Limit();
		@Getter(onMethod = @__(@ThriftField(3)))
		@Setter(onMethod = @__(@ThriftField))
		private Limit mem = new Limit();
		@Getter(onMethod = @__(@ThriftField(4)))
		@Setter(onMethod = @__(@ThriftField))
		private Limit data = new Limit();
		@Getter(onMethod = @__(@ThriftField(5)))
		@Setter(onMethod = @__(@ThriftField))
		private Limit core = new Limit();
		@Getter(onMethod = @__(@ThriftField(6)))
		@Setter(onMethod = @__(@ThriftField))
		private Limit filesize = new Limit();
	}

	@ThriftStruct
	public static class Limit implements Serializable {
		private static final long serialVersionUID = -7149858217513420363L;

		@Getter(onMethod = @__(@ThriftField(1)))
		@Setter(onMethod = @__(@ThriftField))
		private long current;
		@Getter(onMethod = @__(@ThriftField(2)))
		@Setter(onMethod = @__(@ThriftField))
		private long max;
	}

	@ThriftStruct
	public static class JvmInfo implements Serializable {
		private static final long serialVersionUID = -6863013530457150304L;

		private static JvmInfo INSTANCE;

		static {
			RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
			MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

			// returns the <process id>@<host>
			long pid;
			String xPid = runtimeMXBean.getName();
			try {
				xPid = xPid.split("@")[0];
				pid = Long.parseLong(xPid);
			} catch (Exception e) {
				pid = -1;
			}
			JvmInfo info = new JvmInfo();
			info.pid = pid;
			info.startTime = runtimeMXBean.getStartTime();
			info.version = runtimeMXBean.getSystemProperties().get("java.version");
			info.vmName = runtimeMXBean.getVmName();
			info.vmVendor = runtimeMXBean.getVmVendor();
			info.vmVersion = runtimeMXBean.getVmVersion();
			info.mem = new JvmMemory();
			info.mem.heapInit = new ByteSizeValue(memoryMXBean.getHeapMemoryUsage().getInit() < 0 ? 0 : memoryMXBean.getHeapMemoryUsage().getInit());
			info.mem.heapMax = new ByteSizeValue(memoryMXBean.getHeapMemoryUsage().getMax() < 0 ? 0 : memoryMXBean.getHeapMemoryUsage().getMax());
			info.mem.nonHeapInit = new ByteSizeValue(memoryMXBean.getNonHeapMemoryUsage().getInit() < 0 ? 0 : memoryMXBean.getNonHeapMemoryUsage().getInit());
			info.mem.nonHeapMax = new ByteSizeValue(memoryMXBean.getNonHeapMemoryUsage().getMax() < 0 ? 0 : memoryMXBean.getNonHeapMemoryUsage().getMax());
			try {
				Class<?> vmClass = Class.forName("sun.misc.VM");
				info.mem.directMemoryMax = new ByteSizeValue((Long) vmClass.getMethod("maxDirectMemory").invoke(null));
			} catch (Throwable t) {
				// ignore
			}
			info.inputArguments = runtimeMXBean.getInputArguments();
			// info.bootClassPath = runtimeMXBean.getBootClassPath();
			// info.classPath = runtimeMXBean.getClassPath();
			// info.systemProperties = runtimeMXBean.getSystemProperties();

			info.gcCollectors = ManagementFactory.getGarbageCollectorMXBeans().stream().map(gcMxBean -> gcMxBean.getName()).collect(Collectors.toList());
			info.memoryPools = ManagementFactory.getMemoryPoolMXBeans().stream().map(memoryPoolMXBean -> memoryPoolMXBean.getName())
					.collect(Collectors.toList());

			INSTANCE = info;
		}

		public static JvmInfo jvmInfo() {
			return INSTANCE;
		}

		@Getter(onMethod = @__(@ThriftField(1)))
		@Setter(onMethod = @__(@ThriftField))
		private long pid = -1;

		@Getter(onMethod = @__(@ThriftField(2)))
		@Setter(onMethod = @__(@ThriftField))
		private String version = "";
		@Getter(onMethod = @__(@ThriftField(3)))
		@Setter(onMethod = @__(@ThriftField))
		private String vmName = "";
		@Getter(onMethod = @__(@ThriftField(4)))
		@Setter(onMethod = @__(@ThriftField))
		private String vmVersion = "";
		@Getter(onMethod = @__(@ThriftField(5)))
		@Setter(onMethod = @__(@ThriftField))
		private String vmVendor = "";

		@Getter(onMethod = @__(@ThriftField(6)))
		@Setter(onMethod = @__(@ThriftField))
		private long startTime = -1;

		@Getter(onMethod = @__(@ThriftField(7)))
		@Setter(onMethod = @__(@ThriftField))
		private JvmMemory mem;

		@Getter(onMethod = @__(@ThriftField(8)))
		@Setter(onMethod = @__(@ThriftField))
		private List<String> inputArguments;

		// private String bootClassPath;

		// private String classPath;

		// private Map<String, String> systemProperties;

		@Getter(onMethod = @__(@ThriftField(9)))
		@Setter(onMethod = @__(@ThriftField))
		private List<String> gcCollectors;
		@Getter(onMethod = @__(@ThriftField(10)))
		@Setter(onMethod = @__(@ThriftField))
		private List<String> memoryPools;

		public JvmInfo() {
		}

		public int getVersionAsInteger() {
			try {
				int i = 0;
				String sVersion = "";
				for (; i < version.length(); i++) {
					if (!Character.isDigit(version.charAt(i)) && version.charAt(i) != '.') {
						break;
					}
					if (version.charAt(i) != '.') {
						sVersion += version.charAt(i);
					}
				}
				if (i == 0) {
					return -1;
				}
				return Integer.parseInt(sVersion);
			} catch (Exception e) {
				return -1;
			}
		}

		public int getVersionUpdatePack() {
			try {
				int i = 0;
				String sVersion = "";
				for (; i < version.length(); i++) {
					if (!Character.isDigit(version.charAt(i)) && version.charAt(i) != '.') {
						break;
					}
					if (version.charAt(i) != '.') {
						sVersion += version.charAt(i);
					}
				}
				if (i == 0) {
					return -1;
				}
				Integer.parseInt(sVersion);
				int from;
				if (version.charAt(i) == '_') {
					// 1.7.0_4
					from = ++i;
				} else if (version.charAt(i) == '-' && version.charAt(i + 1) == 'u') {
					// 1.7.0-u2-b21
					i = i + 2;
					from = i;
				} else {
					return -1;
				}
				for (; i < version.length(); i++) {
					if (!Character.isDigit(version.charAt(i)) && version.charAt(i) != '.') {
						break;
					}
				}
				if (from == i) {
					return -1;
				}
				return Integer.parseInt(version.substring(from, i));
			} catch (Exception e) {
				return -1;
			}
		}

		@Data
		@ThriftStruct
		public static class JvmMemory implements Serializable {
			private static final long serialVersionUID = 3934246727825595616L;

			@Getter(onMethod = @__(@ThriftField(1)))
			@Setter(onMethod = @__(@ThriftField))
			private ByteSizeValue heapInit = new ByteSizeValue(0);
			@Getter(onMethod = @__(@ThriftField(2)))
			@Setter(onMethod = @__(@ThriftField))
			private ByteSizeValue heapMax = new ByteSizeValue(0);
			@Getter(onMethod = @__(@ThriftField(3)))
			@Setter(onMethod = @__(@ThriftField))
			private ByteSizeValue nonHeapInit = new ByteSizeValue(0);
			@Getter(onMethod = @__(@ThriftField(4)))
			@Setter(onMethod = @__(@ThriftField))
			private ByteSizeValue nonHeapMax = new ByteSizeValue(0);
			@Getter(onMethod = @__(@ThriftField(5)))
			@Setter(onMethod = @__(@ThriftField))
			private ByteSizeValue directMemoryMax = new ByteSizeValue(0);
		}
	}
}
