package com.github.apetrelli.scafa.http.server.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.HttpServerHandler;

public class HttpServerHandlerAdapter implements HttpHandler {

	private HttpServerHandler handler;

	private HttpRequest currentRequest;

	public HttpServerHandlerAdapter(HttpServerHandler handler) {
		this.handler = handler;
	}

	@Override
	public void onConnect() throws IOException {
	}

	@Override
	public void onDisconnect() {
	}

	@Override
	public void onStart() {
		handler.onStart();
	}

	@Override
	public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler) {
        handler.failed(new UnsupportedOperationException("This is for requests only"), null);
	}

	@Override
	public void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler) {
		currentRequest = request;
		this.handler.onRequestHeader(request, handler);
	}

	@Override
	public void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler) {
		this.handler.onBody(currentRequest, buffer, offset, length, handler);
	}

	@Override
	public void onChunkStart(long totalOffset, long chunkLength, CompletionHandler<Void, Void> handler) {
        handler.completed(null, null); // Go on, nothing to call here.
	}

	@Override
	public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength,
			CompletionHandler<Void, Void> handler) {
		this.handler.onBody(currentRequest, buffer, totalOffset, -1, handler);
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
	public void onEnd(CompletionHandler<Void, Void> handler) {
		// The response has to be served yet, and it should happen with this call.
		this.handler.onRequestEnd(currentRequest, handler);
		currentRequest = null;
	}

}
