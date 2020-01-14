package com.github.apetrelli.scafa.tls;

public class TlsConnectionException extends RuntimeException {

	private static final long serialVersionUID = 7183064318331794657L;

	public TlsConnectionException() {
	}

	public TlsConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TlsConnectionException(String message) {
		super(message);
	}

	public TlsConnectionException(Throwable cause) {
		super(cause);
	}

}
