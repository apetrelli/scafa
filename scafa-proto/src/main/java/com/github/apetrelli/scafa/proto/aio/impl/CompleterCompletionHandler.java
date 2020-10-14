package com.github.apetrelli.scafa.proto.aio.impl;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

public class CompleterCompletionHandler
		implements CompletionHandler<Void, CompletableFuture<Void>> {
	
	public static final CompleterCompletionHandler INSTANCE = new CompleterCompletionHandler();
	
	private CompleterCompletionHandler() {
	}

	@Override
	public void completed(Void result, CompletableFuture<Void> attachment) {
		attachment.complete(null);
	}
	
	@Override
	public void failed(Throwable exc, CompletableFuture<Void> attachment) {
		attachment.completeExceptionally(exc);
	}
}
