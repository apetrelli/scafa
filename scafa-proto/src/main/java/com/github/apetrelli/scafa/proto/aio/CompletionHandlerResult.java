package com.github.apetrelli.scafa.proto.aio;

public class CompletionHandlerResult<V, A> {

	private V result;
	
	private A attachment;

	public CompletionHandlerResult(V result, A attachment) {
		this.result = result;
		this.attachment = attachment;
	}
	
	public V getResult() {
		return result;
	}
	
	public A getAttachment() {
		return attachment;
	}
}
