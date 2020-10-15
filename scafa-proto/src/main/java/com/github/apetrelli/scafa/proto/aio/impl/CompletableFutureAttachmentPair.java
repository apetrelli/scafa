package com.github.apetrelli.scafa.proto.aio.impl;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.CompletionHandlerResult;

public class CompletableFutureAttachmentPair<V, A> {

	private CompletableFuture<CompletionHandlerResult<V, A>> future;
	
	private A attachment;

	public CompletableFutureAttachmentPair(CompletableFuture<CompletionHandlerResult<V, A>> future,
			A attachment) {
		this.future = future;
		this.attachment = attachment;
	}
	
	public CompletableFuture<CompletionHandlerResult<V, A>> getFuture() {
		return future;
	}
	
	public A getAttachment() {
		return attachment;
	}
}
