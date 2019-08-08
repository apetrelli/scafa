package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;

public class ClientPipelineHttpHandler implements HttpHandler {

	private static final HttpClientHandler NULL_HANDLER = new NullHttpClientHandler();

	private ConcurrentLinkedQueue<HttpClientHandler> handlers = new ConcurrentLinkedQueue<>();

	private HttpClientHandler currentHandler = NULL_HANDLER;

	public void add(HttpClientHandler handler) {
		handlers.offer(handler);
	}

	@Override
	public void onConnect() throws IOException {
	}

	@Override
	public void onDisconnect() throws IOException {
		handlers.clear();
		currentHandler = NULL_HANDLER;
	}

	@Override
	public void onStart() {
		currentHandler = handlers.poll();
		if (currentHandler == null) {
			currentHandler = NULL_HANDLER;
		}
		currentHandler.onStart();
	}

	@Override
	public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler) {
		currentHandler.onResponseHeader(response, handler);
	}

	@Override
	public void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler) {
		handler.failed(new UnsupportedOperationException("This is for responses only"), null);
	}

	@Override
	public void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler) {
		currentHandler.onBody(buffer, offset, length, handler);
	}

	@Override
	public void onChunkStart(long totalOffset, long chunkLength, CompletionHandler<Void, Void> handler) {
		handler.completed(null, null); // Go on, nothing to call here.
	}

	@Override
	public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength,
			CompletionHandler<Void, Void> handler) {
		currentHandler.onBody(buffer, totalOffset, -1L, handler);
	}

	@Override
	public void onChunkEnd(CompletionHandler<Void, Void> handler) {
		handler.completed(null, null); // Go on, nothing to call here.
	}

	@Override
	public void onChunkedTransferEnd(CompletionHandler<Void, Void> handler) {
		handler.completed(null, null); // Go on, nothing to call here.
	}

	@Override
	public void onDataToPassAlong(ByteBuffer buffer, CompletionHandler<Void, Void> handler) {
		handler.failed(new UnsupportedOperationException("CONNECT method not supported"), null);
	}

	@Override
	public void onEnd() {
		currentHandler.onEnd();
		currentHandler = NULL_HANDLER;
	}

}
