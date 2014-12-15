package io.mandrel.common.monitor;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.hyperic.sigar.Sigar;

@Resource
@Slf4j
public class SigarService {

	private final Sigar sigar;

	public SigarService() {

		Sigar sigar = null;
		try {
			sigar = new Sigar();
			// call it to make sure the library was loaded
			sigar.getPid();
			log.trace("sigar loaded successfully");
		} catch (Throwable t) {
			log.trace("failed to load sigar", t);
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

	public void info() {

		// sigar.getFileSystemList()
	}
}
