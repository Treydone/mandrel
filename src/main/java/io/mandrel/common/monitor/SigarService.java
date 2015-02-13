package io.mandrel.common.monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.apache.cxf.helpers.IOUtils;
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
			org.springframework.core.io.Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(rl).getResources(
					"classpath*:**/libsigar-*");
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

	public Map<String, Object> infos() throws SigarException {

		Map<String, Object> infos = new HashMap<>();
		infos.put("pid", sigar.getPid());
		infos.put("fqdn", sigar.getFQDN());
		infos.put("hostname", sigar.getNetInfo().getHostName());
		infos.put("uptime", sigar.getUptime().getUptime());

		String[] netInterfaceList = sigar.getNetInterfaceList();
		List<Map<String, Object>> iwh = new ArrayList<>(netInterfaceList.length);
		for (String netInterface : netInterfaceList) {
			// Add net interface
			NetInterfaceConfig config = sigar.getNetInterfaceConfig(netInterface);
			Map<String, Object> netConfig = new HashMap<>();
			netConfig.put("name", config.getName());
			netConfig.put("type", config.getType());
			netConfig.put("address", config.getAddress());

			iwh.add(netConfig);
		}
		infos.put("interfaces", iwh);

		// Add cpu
		Map<String, Object> cpu = new HashMap<>();
		infos.put("cpu", cpu);
		cpu.put("sys", sigar.getThreadCpu().getSys());
		cpu.put("total", sigar.getThreadCpu().getTotal());
		cpu.put("user", sigar.getThreadCpu().getUser());

		// Add mem
		Map<String, Object> mem = new HashMap<>();
		infos.put("mem", mem);
		mem.put("total", sigar.getMem().getTotal());
		mem.put("used", sigar.getMem().getUsed());
		mem.put("free", sigar.getMem().getFree());

		// Add swap
		Map<String, Object> swap = new HashMap<>();
		infos.put("swap", swap);
		swap.put("total", sigar.getSwap().getTotal());
		swap.put("used", sigar.getSwap().getUsed());
		swap.put("free", sigar.getSwap().getFree());

		return infos;
	}
}
