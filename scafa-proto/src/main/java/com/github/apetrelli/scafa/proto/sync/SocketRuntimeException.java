package com.github.apetrelli.scafa.proto.sync;

public class SocketRuntimeException extends IORuntimeException {

	private static final long serialVersionUID = -8382637752982334809L;

	public SocketRuntimeException() {
	}

	public SocketRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SocketRuntimeException(String message) {
		super(message);
	}

	public SocketRuntimeException(Throwable cause) {
		super(cause);
	}
}
