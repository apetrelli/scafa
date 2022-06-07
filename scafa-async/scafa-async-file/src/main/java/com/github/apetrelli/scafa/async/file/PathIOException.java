package com.github.apetrelli.scafa.async.file;

public class PathIOException extends RuntimeException {

	private static final long serialVersionUID = -2128251003199564354L;

	public PathIOException() {
	}

	public PathIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public PathIOException(String message) {
		super(message);
	}

	public PathIOException(Throwable cause) {
		super(cause);
	}
	
}
