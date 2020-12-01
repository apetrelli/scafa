package com.github.apetrelli.scafa.http.sync.composite;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.sync.HttpHandler;

public class CompositeHttpHandler implements HttpHandler {

	public static class PatternHandlerPair {
		private String pattern;

		private HttpHandler handler;

		public PatternHandlerPair(String pattern, HttpHandler handler) {
			this.pattern = pattern;
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
	public void onResponseHeader(HttpResponse response) {
        throw new UnsupportedOperationException("This is for requests only");
	}
	
	@Override
	public void onRequestHeader(HttpRequest request) {
		HttpHandler foundHandler = defaultHandler;
		boolean found = false;
		for (int i = 0; i < pairs.length && !found; i++) {
			if (request.getParsedResource().getResource().matches(pairs[i].pattern)) {
				foundHandler = pairs[i].handler;
				found = true;
			}
		}
		currentHandler = foundHandler;
		currentHandler.onRequestHeader(request);
	}

	@Override
	public void onBody(ByteBuffer buffer, long offset, long length) {
		currentHandler.onBody(buffer, offset, length);
	}
	
	@Override
	public void onChunkStart(long totalOffset, long chunkLength) {
		currentHandler.onChunkStart(totalOffset, chunkLength);
	}

	@Override
	public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
		currentHandler.onChunk(buffer, totalOffset, chunkOffset, chunkLength);
	}

	@Override
	public void onChunkEnd() {
		currentHandler.onChunkEnd();
	}

	@Override
	public void onChunkedTransferEnd() {
		currentHandler.onChunkedTransferEnd();
	}

	@Override
	public void onDataToPassAlong(ByteBuffer buffer) {
		currentHandler.onDataToPassAlong(buffer);
	}

	@Override
	public void onEnd() {
		currentHandler.onEnd();
		currentHandler = defaultHandler;
	}

	@Override
	public void onError(Throwable exc) {
		if (currentHandler != null) {
			currentHandler.onError(exc);
		}
	}
}
