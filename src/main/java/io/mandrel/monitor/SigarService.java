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
import org.hyperic.sigar.NetInterfaceConfig;
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
}
