package com.github.apetrelli.scafa.proto.io;

import com.github.apetrelli.scafa.proto.IORuntimeException;

public class FlowClosedException extends IORuntimeException {

	private static final long serialVersionUID = -7071095461437944958L;

	public FlowClosedException() {
	}

	public FlowClosedException(String message, Throwable cause) {
		super(message, cause);
	}

	public FlowClosedException(String message) {
		super(message);
	}

	public FlowClosedException(Throwable cause) {
		super(cause);
	}
	
}
