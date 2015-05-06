package io.mandrel.common;

public class MandrelException extends RuntimeException {

	private static final long serialVersionUID = -4167895335374331706L;

	public MandrelException() {
		super();
	}

	public MandrelException(String message, Throwable cause) {
		super(message, cause);
	}

	public MandrelException(String message) {
		super(message);
	}

	public MandrelException(Throwable cause) {
		super(cause);
	}

}
