package com.github.apetrelli.scafa.proto.aio.impl;

public class IORuntimeException extends RuntimeException {

	private static final long serialVersionUID = -8836021005290477534L;

	public IORuntimeException() {
	}

	public IORuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public IORuntimeException(String message) {
		super(message);
	}

	public IORuntimeException(Throwable cause) {
		super(cause);
	}

}
