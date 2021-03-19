package com.github.apetrelli.scafa.async.proto.util;

import java.util.concurrent.CompletableFuture;

public class CompletionHandlerFuture {

	private CompletionHandlerFuture() {}
	
	public static CompletableFuture<Void> completeEmpty() {
		return CompletableFuture.completedFuture(null);
	}
}
