package io.mandrel.common;

public class NotFoundException extends MandrelException {

	private static final long serialVersionUID = 7571251717386780042L;

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(String message) {
		super(message);
	}
}
