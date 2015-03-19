package io.mandrel.node;

import io.mandrel.monitor.SigarService;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class NodeTask implements Callable<Map<String, Object>>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6204571043673228240L;

	private transient SigarService sigarService;

	@Autowired
	public void setSigarService(SigarService sigarService) {
		this.sigarService = sigarService;
	}

	@Override
	public Map<String, Object> call() throws Exception {
		return sigarService.infos();
	}
}