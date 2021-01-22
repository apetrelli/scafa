package com.github.apetrelli.scafa.http.async.composite;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.async.HttpHandler;

public class CompositeHttpHandler implements HttpHandler {

	public static class PatternHandlerPair {
		private Pattern pattern;

		private HttpHandler handler;

		public PatternHandlerPair(String pattern, HttpHandler handler) {
			this.pattern = Pattern.compile(pattern);
			this.handler = handler;
		}
	}

	private PatternHandlerPair[] pairs;

	private HttpHandler defaultHandler;

	private HttpHandler currentHandler;

	public CompositeHttpHandler(HttpHandler defaultHandler, PatternHandlerPair... pairs) {
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
			if (pairs[i].pattern.matcher(request.getParsedResource().getResource()).matches()) {
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

	@Override
	public void onError(Throwable exc) {
		if (currentHandler != null) {
			currentHandler.onError(exc);
		}
	}
}
