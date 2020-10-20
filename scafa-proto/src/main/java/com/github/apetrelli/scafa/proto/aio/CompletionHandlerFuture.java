package com.github.apetrelli.scafa.proto.aio;

import java.util.concurrent.CompletableFuture;

public class CompletionHandlerFuture {

	private CompletionHandlerFuture() {}
	
	public static CompletableFuture<Void> completeEmpty() {
		return CompletableFuture.completedFuture(null);
	}
}
