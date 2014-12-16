package io.mandrel.common.monitor;

import org.hyperic.sigar.SigarException;
import org.junit.Test;

public class SigarServiceTest {

	private final SigarService sigarService = new SigarService();

	@Test
	public void test() throws SigarException {

		sigarService.info();
	}
}
