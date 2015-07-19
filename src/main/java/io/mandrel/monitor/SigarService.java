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

import io.mandrel.monitor.Infos.Interface;
import io.mandrel.stats.JmxUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SigarService {

	private final Sigar sigar;

	@Inject
	public SigarService(ResourceLoader rl) {

		try {
			org.springframework.core.io.Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(rl).getResources("classpath*:**/libsigar-*");
			for (org.springframework.core.io.Resource resource : resources) {
				log.debug("Loading native file {}", resource.getFilename());
				try {
					File temp = File.createTempFile(resource.getFilename(), "");
					temp.deleteOnExit();

					if (!temp.exists()) {
						throw new FileNotFoundException("File " + resource.getFilename() + " can not be found");
					}

					try (FileOutputStream stream = new FileOutputStream(temp)) {
						IOUtils.copy(resource.getInputStream(), stream);
					}

					System.load(temp.getAbsolutePath());
				} catch (UnsatisfiedLinkError e) {
				}
			}
		} catch (IOException e) {
			log.warn("Unable to find the natives libsigar", e);
		}

		Sigar sigar = null;
		try {
			sigar = new Sigar();
			// call it to make sure the library was loaded
			sigar.getPid();
			log.debug("sigar loaded successfully");
		} catch (Throwable t) {
			log.debug("failed to load sigar", t);
			if (sigar != null) {
				try {
					sigar.close();
				} catch (Throwable t1) {
					// ignore
				} finally {
					sigar = null;
				}
			}
		}
		this.sigar = sigar;
	}

	public Infos infos() {

		Infos infos = new Infos();
		try {
			infos.setPid(sigar.getPid());
			infos.setFqdn(sigar.getFQDN());
			infos.setHostname(sigar.getNetInfo().getHostName());
			infos.setUptime(sigar.getUptime().getUptime());

			String[] netInterfaceList = sigar.getNetInterfaceList();
			for (String netInterfaceName : netInterfaceList) {
				// Add net interface
				NetInterfaceConfig config = sigar.getNetInterfaceConfig(netInterfaceName);
				Interface netInterface = new Interface();
				netInterface.setName(config.getName());
				netInterface.setType(config.getType());
				netInterface.setAddress(config.getAddress());
				infos.getInterfaces().add(netInterface);
			}

			// Add cpu
			infos.getCpu().setSys(sigar.getThreadCpu().getSys());
			infos.getCpu().setTotal(sigar.getThreadCpu().getTotal());
			infos.getCpu().setUser(sigar.getThreadCpu().getUser());

			// Add mem
			infos.getMem().setTotal(sigar.getMem().getTotal());
			infos.getMem().setUsed(sigar.getMem().getUsed());
			infos.getMem().setFree(sigar.getMem().getFree());

			// Add swap
			infos.getSwap().setTotal(sigar.getSwap().getTotal());
			infos.getSwap().setUsed(sigar.getSwap().getUsed());
			infos.getSwap().setFree(sigar.getSwap().getFree());

			// Add limits
			infos.getLimits().getOpenfiles().setCurrent(JmxUtils.getOpenFileDescriptorCount());
			infos.getLimits().getOpenfiles().setMax(JmxUtils.getMaxFileDescriptorCount());
			// infos.getLimits().getOpenfiles().setCurrent(sigar.getResourceLimit().getOpenFilesCur());
			// infos.getLimits().getOpenfiles().setMax(sigar.getResourceLimit().getOpenFilesMax());

			infos.getLimits().getCpu().setCurrent(sigar.getResourceLimit().getCpuCur());
			infos.getLimits().getCpu().setMax(sigar.getResourceLimit().getCpuMax());

			infos.getLimits().getData().setCurrent(sigar.getResourceLimit().getDataCur());
			infos.getLimits().getData().setMax(sigar.getResourceLimit().getDataMax());

			infos.getLimits().getCore().setCurrent(sigar.getResourceLimit().getCoreCur());
			infos.getLimits().getCore().setMax(sigar.getResourceLimit().getCoreMax());

			infos.getLimits().getFilesize().setCurrent(sigar.getResourceLimit().getFileSizeCur());
			infos.getLimits().getFilesize().setMax(sigar.getResourceLimit().getFileSizeMax());

			infos.getLimits().getMem().setCurrent(sigar.getResourceLimit().getMemoryCur());
			infos.getLimits().getMem().setMax(sigar.getResourceLimit().getMemoryMax());

		} catch (SigarException e) {
			// TODO ...
			e.printStackTrace();
		}

		return infos;
	}
	
    public String ifconfig() {
        StringBuilder sb = new StringBuilder();
        try {
            for (String ifname : sigar.getNetInterfaceList()) {
                NetInterfaceConfig ifconfig = null;
                try {
                    ifconfig = sigar.getNetInterfaceConfig(ifname);
                } catch (SigarException e) {
                    sb.append(ifname + "\t" + "Not Avaialbe [" + e.getMessage() + "]");
                    continue;
                }
                long flags = ifconfig.getFlags();

                String hwaddr = "";
                if (!NetFlags.NULL_HWADDR.equals(ifconfig.getHwaddr())) {
                    hwaddr = " HWaddr " + ifconfig.getHwaddr();
                }

                if (!ifconfig.getName().equals(ifconfig.getDescription())) {
                    sb.append(ifconfig.getDescription()).append('\n');
                }

                sb.append(ifconfig.getName() + "\t" + "Link encap:" + ifconfig.getType() + hwaddr).append('\n');

                String ptp = "";
                if ((flags & NetFlags.IFF_POINTOPOINT) > 0) {
                    ptp = "  P-t-P:" + ifconfig.getDestination();
                }

                String bcast = "";
                if ((flags & NetFlags.IFF_BROADCAST) > 0) {
                    bcast = "  Bcast:" + ifconfig.getBroadcast();
                }

                sb.append("\t" +
                        "inet addr:" + ifconfig.getAddress() +
                        ptp + //unlikely
                        bcast +
                        "  Mask:" + ifconfig.getNetmask()).append('\n');

                sb.append("\t" +
                        NetFlags.getIfFlagsString(flags) +
                        " MTU:" + ifconfig.getMtu() +
                        "  Metric:" + ifconfig.getMetric()).append('\n');
                try {
                    NetInterfaceStat ifstat = sigar.getNetInterfaceStat(ifname);

                    sb.append("\t" +
                            "RX packets:" + ifstat.getRxPackets() +
                            " errors:" + ifstat.getRxErrors() +
                            " dropped:" + ifstat.getRxDropped() +
                            " overruns:" + ifstat.getRxOverruns() +
                            " frame:" + ifstat.getRxFrame()).append('\n');

                    sb.append("\t" +
                            "TX packets:" + ifstat.getTxPackets() +
                            " errors:" + ifstat.getTxErrors() +
                            " dropped:" + ifstat.getTxDropped() +
                            " overruns:" + ifstat.getTxOverruns() +
                            " carrier:" + ifstat.getTxCarrier()).append('\n');
                    sb.append("\t" + "collisions:" +
                            ifstat.getTxCollisions()).append('\n');

                    long rxBytes = ifstat.getRxBytes();
                    long txBytes = ifstat.getTxBytes();

                    sb.append("\t" +
                            "RX bytes:" + rxBytes +
                            " (" + Sigar.formatSize(rxBytes) + ")" +
                            "  " +
                            "TX bytes:" + txBytes +
                            " (" + Sigar.formatSize(txBytes) + ")").append('\n');
                } catch (SigarException e) {
                }
            }
            return sb.toString();
        } catch (SigarException e) {
            return "NA";
        }
    }
}
