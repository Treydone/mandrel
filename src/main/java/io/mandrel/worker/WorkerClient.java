package io.mandrel.worker;

import io.mandrel.common.data.Spider;
import io.mandrel.common.thrift.TClient;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.netflix.servo.util.Throwables;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WorkerClient {

	private TClient client;

	public void create(Spider spider) {
		try {
			client.frontierClient().create(null);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}

	public void start(Long spiderId) {
		try {
			client.frontierClient().start(spiderId);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}

	public void pause(Long spiderId) {
		try {
			client.frontierClient().pause(spiderId);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}

	public void kill(Long spiderId) {
		try {
			client.frontierClient().kill(spiderId);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}
}
