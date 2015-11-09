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
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Infos implements Serializable {
	private static final long serialVersionUID = 3436344116385574896L;

	private long pid;
	private String fqdn;
	private String hostname;
	private double uptime;

	private Cpu cpu = new Cpu();
	private Mem mem = new Mem();
	private Swap swap = new Swap();
	private List<Interface> interfaces = new ArrayList<>(2);
	private Limits limits = new Limits();
	private JvmInfo jvmInfo = JvmInfo.jvmInfo();

	@Data
	public static class Cpu implements Serializable {
		private static final long serialVersionUID = 8512684069342496081L;

		private long sys;
		private long total;
		private long user;
	}

	@Data
	public static class Mem implements Serializable {
		private static final long serialVersionUID = -8797648466105477814L;

		private long total;
		private long used;
		private long free;
	}

	@Data
	public static class Swap implements Serializable {
		private static final long serialVersionUID = 1409517375142998802L;

		private long total;
		private long used;
		private long free;
	}

	@Data
	public static class Interface implements Serializable {
		private static final long serialVersionUID = 49989037162348232L;

		private String name;
		private String type;
		private String address;
	}

	@Data
	public static class Limits implements Serializable {
		private static final long serialVersionUID = 7797483638794413832L;

		private Limit openfiles = new Limit();
		private Limit cpu = new Limit();
		private Limit mem = new Limit();
		private Limit data = new Limit();
		private Limit core = new Limit();
		private Limit filesize = new Limit();
	}

	@Data
	public static class Limit implements Serializable {
		private static final long serialVersionUID = -7149858217513420363L;

		private long current;
		private long max;
	}

	@Data
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
			info.inputArguments = runtimeMXBean.getInputArguments().toArray(new String[runtimeMXBean.getInputArguments().size()]);
			// info.bootClassPath = runtimeMXBean.getBootClassPath();
			// info.classPath = runtimeMXBean.getClassPath();
			// info.systemProperties = runtimeMXBean.getSystemProperties();

			List<GarbageCollectorMXBean> gcMxBeans = ManagementFactory.getGarbageCollectorMXBeans();
			info.gcCollectors = new String[gcMxBeans.size()];
			for (int i = 0; i < gcMxBeans.size(); i++) {
				GarbageCollectorMXBean gcMxBean = gcMxBeans.get(i);
				info.gcCollectors[i] = gcMxBean.getName();
			}

			List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
			info.memoryPools = new String[memoryPoolMXBeans.size()];
			for (int i = 0; i < memoryPoolMXBeans.size(); i++) {
				MemoryPoolMXBean memoryPoolMXBean = memoryPoolMXBeans.get(i);
				info.memoryPools[i] = memoryPoolMXBean.getName();
			}

			INSTANCE = info;
		}

		public static JvmInfo jvmInfo() {
			return INSTANCE;
		}

		private long pid = -1;

		private String version = "";
		private String vmName = "";
		private String vmVersion = "";
		private String vmVendor = "";

		private long startTime = -1;

		private JvmMemory mem;

		private String[] inputArguments;

		// private String bootClassPath;

		// private String classPath;

		// private Map<String, String> systemProperties;

		private String[] gcCollectors = new String[0];
		private String[] memoryPools = new String[0];

		private JvmInfo() {
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
		public static class JvmMemory implements Serializable {
			private static final long serialVersionUID = 3934246727825595616L;

			private ByteSizeValue heapInit = new ByteSizeValue(0);
			private ByteSizeValue heapMax = new ByteSizeValue(0);
			private ByteSizeValue nonHeapInit = new ByteSizeValue(0);
			private ByteSizeValue nonHeapMax = new ByteSizeValue(0);
			private ByteSizeValue directMemoryMax = new ByteSizeValue(0);
		}
	}
}
