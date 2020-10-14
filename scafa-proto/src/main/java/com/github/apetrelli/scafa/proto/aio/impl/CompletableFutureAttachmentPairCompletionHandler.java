package com.github.apetrelli.scafa.proto.aio.impl;

import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.aio.CompletionHandlerResult;

public class CompletableFutureAttachmentPairCompletionHandler<V, A>
		implements CompletionHandler<V, CompletableFutureAttachmentPair<V, A>> {
	
	@SuppressWarnings("rawtypes")
	private static final CompletableFutureAttachmentPairCompletionHandler INSTANCE = new CompletableFutureAttachmentPairCompletionHandler(); // NOSONAR
	
	@SuppressWarnings("unchecked")
	public static final <V, A> CompletableFutureAttachmentPairCompletionHandler<V, A> getInstance() {
		return INSTANCE;
	}
	
	private CompletableFutureAttachmentPairCompletionHandler() {
	}

	@Override
	public void completed(V result, CompletableFutureAttachmentPair<V, A> attachment) {
		attachment.getFuture().complete(new CompletionHandlerResult<>(result, attachment.getAttachment()));
	}

	@Override
	public void failed(Throwable exc, CompletableFutureAttachmentPair<V, A> attachment) {
		attachment.getFuture().completeExceptionally(new CompletionHandlerException(attachment.getAttachment(), exc));
	}

}
