package io.mandrel.service.spider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Spider {

	private int requestTimeOut;

	private Map<String, Collection<String>> headers;

	private Map<String, List<String>> params;

	public int getRequestTimeOut() {
		return requestTimeOut;
	}

	public void setRequestTimeOut(int requestTimeOut) {
		this.requestTimeOut = requestTimeOut;
	}

	public Map<String, Collection<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Collection<String>> headers) {
		this.headers = headers;
	}

	public Map<String, List<String>> getParams() {
		return params;
	}

	public void setParams(Map<String, List<String>> params) {
		this.params = params;
	}

}
