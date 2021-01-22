package com.github.apetrelli.scafa.async.proto.aio;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

public final class CompletableFutureCompletionHandler
		implements CompletionHandler<Integer, CompletableFuture<Integer>> {

	private static final CompletableFutureCompletionHandler INSTANCE = new CompletableFutureCompletionHandler();
	
	public static CompletableFutureCompletionHandler getInstance() {
		return INSTANCE;
	}
	
	private CompletableFutureCompletionHandler() {
	}
	
	public void completed(Integer result, CompletableFuture<Integer> attachment) {
		attachment.complete(result);
	}

	public void failed(Throwable exc, CompletableFuture<Integer> attachment) {
		attachment.completeExceptionally(exc);
	}
}