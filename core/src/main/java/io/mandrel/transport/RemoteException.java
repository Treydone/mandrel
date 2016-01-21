package io.mandrel.transport;

import io.mandrel.common.MandrelException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class RemoteException extends MandrelException {
	private static final long serialVersionUID = 2420782417683971997L;

	@Getter(onMethod = @__(@ThriftField(0)))
	@Setter(onMethod = @__(@ThriftField))
	private Error error;
	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private String details;

	public static enum Error {

		// Global
		G_UNKNOWN,

		// Frontier
		// F_TIMEOUT_WHEN_GETTING_URI
	}

	@Override
	public String getMessage() {
		return error.toString() + ": " + details;
	}
}
