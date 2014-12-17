package io.mandrel.common.monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.apache.cxf.helpers.IOUtils;
import org.hyperic.sigar.FileSystem;
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
			org.springframework.core.io.Resource[] resources = ResourcePatternUtils
					.getResourcePatternResolver(rl).getResources(
							"classpath*:**/libsigar-*");
			for (org.springframework.core.io.Resource resource : resources) {
				log.debug("Loading native file {}", resource.getFilename());
				try {
					File temp = File.createTempFile(resource.getFilename(), "");
					temp.deleteOnExit();

					if (!temp.exists()) {
						throw new FileNotFoundException("File "
								+ resource.getFilename() + " can not be found");
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

	public void info() throws SigarException {

		for (FileSystem fs : sigar.getFileSystemList()) {
			System.err.println(fs.toString());
		}

		System.err.println(sigar.getFQDN());
	}
}
