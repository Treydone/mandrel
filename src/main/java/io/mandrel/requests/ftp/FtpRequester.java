package io.mandrel.requests.ftp;

import io.mandrel.blob.Blob;
import io.mandrel.common.data.FtpStrategy;
import io.mandrel.common.data.Spider;
import io.mandrel.requests.Requester;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class FtpRequester extends Requester {

	private static final long serialVersionUID = 6496471430026028585L;

	@JsonProperty("strategy")
	private FtpStrategy strategy;

	@Override
	public void close() throws IOException {
	}

	@Override
	public void init() {
	}

	@Override
	public void get(URI uri, Spider spider, SuccessCallback successCallback, FailureCallback failureCallback) {
	}

	@Override
	public Blob getBlocking(URI uri, Spider spider) throws Exception {
		return null;
	}

	@Override
	public Blob getBlocking(URI uri) throws Exception {
		return null;
	}

	@Override
	public String name() {
		return "ftp";
	}

	@Override
	public Set<String> getProtocols() {
		return Sets.newHashSet("ftp", "ftps");
	}
}
