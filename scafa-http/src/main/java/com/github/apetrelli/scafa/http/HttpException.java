package com.github.apetrelli.scafa.http;

public class HttpException extends RuntimeException {

	private static final long serialVersionUID = -5624852304210316727L;

	public HttpException() {
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable cause) {
		super(cause);
	}

}
