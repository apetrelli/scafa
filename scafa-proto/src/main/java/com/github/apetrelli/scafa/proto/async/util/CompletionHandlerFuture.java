package com.github.apetrelli.scafa.proto.async.util;

import java.util.concurrent.CompletableFuture;

public class CompletionHandlerFuture {

	private CompletionHandlerFuture() {}
	
	public static CompletableFuture<Void> completeEmpty() {
		return CompletableFuture.completedFuture(null);
	}
}
