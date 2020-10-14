package com.github.apetrelli.scafa.proto.aio;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.impl.CompletionHandlerException;

public class CompletionHandlerFuture {

	private CompletionHandlerFuture() {}
	
	public static <V, A> CompletableFuture<CompletionHandlerResult<V, A>> complete(V result, A attachment) {
		return CompletableFuture.completedFuture(new CompletionHandlerResult<V, A>(result, attachment));
	}
	
	public static <V, A> CompletableFuture<CompletionHandlerResult<V, A>> failed(Throwable exc, A attachment) {
		return CompletableFuture.failedFuture(new CompletionHandlerException(attachment, exc));
	}
	
	public static CompletableFuture<Void> completeEmpty() {
		return CompletableFuture.completedFuture(null);
	}
}
