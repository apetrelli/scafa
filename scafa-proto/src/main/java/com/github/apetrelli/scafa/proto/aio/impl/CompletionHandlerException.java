package com.github.apetrelli.scafa.proto.aio.impl;

public class CompletionHandlerException extends RuntimeException {

	private static final long serialVersionUID = 1207701448627903458L;
	
	private final transient Object attachment;
	
	public CompletionHandlerException(Object attachment, Throwable throwable) {
		super(throwable);
		this.attachment = attachment;
	}

	@SuppressWarnings("unchecked")
	public <A> A getAttachment() {
		return (A) attachment;
	}
}
