package io.mandrel.controller;

import io.mandrel.common.data.Spider;
import io.mandrel.common.thrift.TClient;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.netflix.servo.util.Throwables;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ControllerClient {

	private TClient client;

	public void create(Spider spider) {
		// TODO ??
	}

	public void start(Long spiderId) {
		try {
			client.controllerClient().start(spiderId);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}

	public void pause(Long spiderId) {
		try {
			client.controllerClient().pause(spiderId);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}

	public void kill(Long spiderId) {
		try {
			client.controllerClient().kill(spiderId);
		} catch (TException e) {
			throw Throwables.propagate(e);
		}
	}
}
