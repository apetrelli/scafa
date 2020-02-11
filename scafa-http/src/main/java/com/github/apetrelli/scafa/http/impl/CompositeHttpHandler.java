package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;

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
	public void onConnect() throws IOException {
	}

	@Override
	public void onDisconnect() {
	}

	@Override
	public void onStart() {
		currentHandler = defaultHandler;
	}

	@Override
	public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler) {
        handler.failed(new UnsupportedOperationException("This is for requests only"), null);
	}

	@Override
	public void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler) {
		HttpHandler foundHandler = defaultHandler;
		boolean found = false;
		for (int i = 0; i < pairs.length && !found; i++) {
			if (request.getParsedResource().getResource().matches(pairs[i].pattern)) {
				foundHandler = pairs[i].handler;
				found = true;
			}
		}
		currentHandler = foundHandler;
		currentHandler.onRequestHeader(request, handler);
	}

	@Override
	public void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler) {
		currentHandler.onBody(buffer, offset, length, handler);
	}

	@Override
	public void onChunkStart(long totalOffset, long chunkLength, CompletionHandler<Void, Void> handler) {
		currentHandler.onChunkStart(totalOffset, chunkLength, handler);
	}

	@Override
	public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength,
			CompletionHandler<Void, Void> handler) {
		currentHandler.onChunk(buffer, totalOffset, chunkOffset, chunkLength, handler);
	}

	@Override
	public void onChunkEnd(CompletionHandler<Void, Void> handler) {
		currentHandler.onChunkEnd(handler);
	}

	@Override
	public void onChunkedTransferEnd(CompletionHandler<Void, Void> handler) {
		currentHandler.onChunkedTransferEnd(handler);
	}

	@Override
	public void onDataToPassAlong(ByteBuffer buffer, CompletionHandler<Void, Void> handler) {
		currentHandler.onDataToPassAlong(buffer, handler);
	}

	@Override
	public void onEnd(CompletionHandler<Void, Void> handler) {
		currentHandler.onEnd(handler);
		currentHandler = defaultHandler;
	}

}
