package com.github.apetrelli.scafa.http.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;

public class CompositeHttpHandler implements HttpHandler {

	public static class CompositeHttpHandlerBuilder {
		private HttpHandler defaultHandler;

		private List<PatternHandlerPair> pairs = new ArrayList<>();

		public CompositeHttpHandler build() {
			return new CompositeHttpHandler(defaultHandler, pairs.toArray(new PatternHandlerPair[pairs.size()]));
		}

		public CompositeHttpHandlerBuilder withDefaultHandler(HttpHandler defaultHandler) {
			this.defaultHandler = defaultHandler;
			return this;
		}

		public CompositeHttpHandlerBuilder withHandler(String pattern, HttpHandler handler) {
			pairs.add(new PatternHandlerPair(pattern, handler));
			return this;
		}
	}

	private static class PatternHandlerPair {
		private String pattern;

		private HttpHandler handler;

		public PatternHandlerPair(String pattern, HttpHandler handler) {
			this.pattern = pattern;
			this.handler = handler;
		}
	}

	public static CompositeHttpHandlerBuilder builder() {
		return new CompositeHttpHandlerBuilder();
	}

	private PatternHandlerPair[] pairs;

	private HttpHandler defaultHandler;

	private HttpHandler currentHandler;

	private CompositeHttpHandler(HttpHandler defaultHandler, PatternHandlerPair... pairs) {
		this.defaultHandler = defaultHandler;
		this.pairs = pairs;
	}

	@Override
	public void onConnect() {
		// Does nothing
	}

	@Override
	public void onDisconnect() {
		// Does nothing
	}

	@Override
	public void onStart() {
		currentHandler = defaultHandler;
	}
	
	@Override
	public CompletableFuture<Void> onResponseHeader(HttpResponse response) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("This is for requests only"));
	}
	
	@Override
	public CompletableFuture<Void> onRequestHeader(HttpRequest request) {
		HttpHandler foundHandler = defaultHandler;
		boolean found = false;
		for (int i = 0; i < pairs.length && !found; i++) {
			if (request.getParsedResource().getResource().matches(pairs[i].pattern)) {
				foundHandler = pairs[i].handler;
				found = true;
			}
		}
		currentHandler = foundHandler;
		return currentHandler.onRequestHeader(request);
	}

	@Override
	public CompletableFuture<Void> onBody(ByteBuffer buffer, long offset, long length) {
		return currentHandler.onBody(buffer, offset, length);
	}
	
	@Override
	public CompletableFuture<Void> onChunkStart(long totalOffset, long chunkLength) {
		return currentHandler.onChunkStart(totalOffset, chunkLength);
	}

	@Override
	public CompletableFuture<Void> onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
		return currentHandler.onChunk(buffer, totalOffset, chunkOffset, chunkLength);
	}

	@Override
	public CompletableFuture<Void> onChunkEnd() {
		return currentHandler.onChunkEnd();
	}

	@Override
	public CompletableFuture<Void> onChunkedTransferEnd() {
		return currentHandler.onChunkedTransferEnd();
	}

	@Override
	public CompletableFuture<Void> onDataToPassAlong(ByteBuffer buffer) {
		return currentHandler.onDataToPassAlong(buffer);
	}

	@Override
	public CompletableFuture<Void> onEnd() {
		return currentHandler.onEnd().thenRun(() -> currentHandler = defaultHandler);
	}

}
