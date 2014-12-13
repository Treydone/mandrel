package ohscrap.common;

import java.util.Collection;
import java.util.Map;

public class Spider {

	private int requestTimeOut;

	private Map<String, Collection<String>> headers;

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

}
