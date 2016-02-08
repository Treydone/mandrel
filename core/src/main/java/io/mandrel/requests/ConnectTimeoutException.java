package io.mandrel.requests;

public class ConnectTimeoutException extends RequestException {

	public ConnectTimeoutException() {
		super();
	}

	public ConnectTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectTimeoutException(String message) {
		super(message);
	}

	public ConnectTimeoutException(Throwable cause) {
		super(cause);
	}

}
