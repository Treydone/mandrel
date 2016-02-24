/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.common;

import io.mandrel.common.logging.LoggerMessageFormat;

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

/**
	 * Construct a <code>MandrelException</code> with the specified detail
	 * message.
	 *
	 * The message can be parameterized using {@code as placeholders for the
	 * given arguments.
	 *
	 * @param msg
	 *            the detail message
	 * @param args
	 *            the arguments for the message
	 */
	public MandrelException(String msg, Object... args) {
		super(LoggerMessageFormat.format(msg, args));
	}

/**
	 * Construct a <code>MandrelException</code> with the specified detail
	 * message and nested exception.
	 *
	 * The message can be parameterized using {@code as placeholders for the
	 * given arguments.
	 *
	 * @param msg 
	 *            the detail message
	 * @param cause
	 *            the nested exception
	 * @param args
	 *            the arguments for the message
	 */
	public MandrelException(String msg, Throwable cause, Object... args) {
		super(LoggerMessageFormat.format(msg, args), cause);
	}

}
