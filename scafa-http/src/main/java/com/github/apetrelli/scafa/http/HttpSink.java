package com.github.apetrelli.scafa.http;

import java.util.concurrent.CompletableFuture;

public interface HttpSink {
	CompletableFuture<Void> endHeader(HttpProcessingContext context, HttpHandler handler);
	
	CompletableFuture<Void> endHeaderAndRequest(HttpProcessingContext context, HttpHandler handler);
	
	CompletableFuture<Void> data(HttpProcessingContext context, HttpHandler handler);
	
	CompletableFuture<Void> chunkData(HttpProcessingContext context, HttpHandler handler);
	
	CompletableFuture<Void> endChunkCount(HttpProcessingContext context, HttpHandler handler);
}
