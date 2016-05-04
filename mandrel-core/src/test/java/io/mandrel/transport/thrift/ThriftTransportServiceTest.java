package io.mandrel.transport.thrift;

import io.mandrel.cluster.discovery.Service;
import io.mandrel.cluster.discovery.local.LocalDiscoveryClient;
import io.mandrel.cluster.node.Node;
import io.mandrel.endpoints.contracts.NodeContract;
import io.mandrel.transport.TransportProperties;

import java.util.Arrays;

import org.junit.Test;

public class ThriftTransportServiceTest {

	@Test
	public void test() {

		ThriftTransportService thriftTransportService = new ThriftTransportService();
		thriftTransportService.setClient(new ThriftClient());
		thriftTransportService.setDiscoveryClient(new LocalDiscoveryClient());
		thriftTransportService.setProperties(new ThriftTransportProperties());
		thriftTransportService.setResources(Arrays.asList(new NodeContract() {
			@Override
			public void close() throws Exception {

			}

			@Override
			public void shutdown() {

			}

			@Override
			public Node dhis() {
				return null;
			}
		}));
		thriftTransportService.setServices(Arrays.asList(new Service() {
			@Override
			public String getServiceName() {
				return "toto";
			}
		}));
		thriftTransportService.setTransportProperties(new TransportProperties());

		thriftTransportService.init();
	}
}
