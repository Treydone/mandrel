package io.mandrel.endpoints.contracts;

import org.springframework.validation.BindingResult;

public class BindException extends org.springframework.validation.BindException {

	private static final long serialVersionUID = -4987543836991676392L;

	public BindException(Object target, String objectName) {
		super(target, objectName);
	}

	public BindException(BindingResult bindingResult) {
		super(bindingResult);
	}
}
