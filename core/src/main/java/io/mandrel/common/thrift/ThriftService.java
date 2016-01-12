package io.mandrel.common.thrift;

import io.mandrel.endpoints.contracts.Contract;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.facebook.nifty.core.NiftyTimer;
import com.facebook.nifty.processor.NiftyProcessor;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.service.ThriftServiceProcessor;
import com.facebook.swift.service.ThriftServiceStatsHandler;

@Component
public class ThriftService {

	@Autowired
	private List<? extends Contract> resources;

	@Autowired
	private ThriftServerProperties properties;

	@Value("${standalone:true}")
	private boolean local;

	private ThriftServer server;

	@PostConstruct
	public void init() {
		NiftyProcessor processor = new ThriftServiceProcessor(new ThriftCodecManager(), Arrays.asList(new ThriftServiceStatsHandler()), resources);

		server = new ThriftServer(processor, properties, new NiftyTimer("thrift"), ThriftServer.DEFAULT_FRAME_CODEC_FACTORIES,
				ThriftServer.DEFAULT_PROTOCOL_FACTORIES, ThriftServer.DEFAULT_WORKER_EXECUTORS, ThriftServer.DEFAULT_SECURITY_FACTORY, local);
		server.start();
	}

	@PreDestroy
	public void destroy() {
		server.close();
	}
}
