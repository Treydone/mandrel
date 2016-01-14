package io.mandrel.transport.thrift;

public class TClientException extends RuntimeException {

	// Fucking Eclipse
	private static final long serialVersionUID = -2275296727467192665L;

	public TClientException(String message, Exception e) {
		super(message, e);
	}

}
