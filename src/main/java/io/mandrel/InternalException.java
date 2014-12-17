package io.mandrel;

public class InternalException extends RuntimeException {

	public InternalException() {
		super();
	}

	public InternalException(Exception e) {
		super(e);
	}
}
