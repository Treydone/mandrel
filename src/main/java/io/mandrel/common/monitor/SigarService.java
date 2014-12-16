package io.mandrel.common.monitor;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

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

	public void info() throws SigarException {

		for (FileSystem fs : sigar.getFileSystemList()) {
			System.err.println(fs.toString());
		}
		
		System.err.println(sigar.getFQDN());
	}
}
