package com.github.apetrelli.scafa.proto.sync;

public class IORuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1368583784478868202L;

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
