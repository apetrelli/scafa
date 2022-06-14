package com.github.apetrelli.scafa.async.http.server.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;

import lombok.RequiredArgsConstructor;

import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.http.impl.HttpHandlerSupport;
import com.github.apetrelli.scafa.async.http.server.HttpServerHandler;

@RequiredArgsConstructor
public class HttpServerHandlerAdapter extends HttpHandlerSupport implements HttpHandler {

	private final HttpServerHandler handler;

	private HttpRequest currentRequest;

	@Override
	public void onStart() {
		handler.onStart();
	}
	
	@Override
	public CompletableFuture<Void> onResponseHeader(HttpResponse response) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("This is for requests only"));
	}
	
	@Override
	public CompletableFuture<Void> onRequestHeader(HttpRequest request) {
		currentRequest = request;
		return this.handler.onRequestHeader(request);
	}
	
	@Override
	public CompletableFuture<Void> onBody(ByteBuffer buffer, long offset, long length) {
		return this.handler.onBody(currentRequest, buffer, offset, length);
	}

	@Override
	public CompletableFuture<Void> onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
		return this.handler.onBody(currentRequest, buffer, totalOffset, -1);
	}
	
	@Override
	public CompletableFuture<Void> onDataToPassAlong(ByteBuffer buffer) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("CONNECT method not supported"));
	}

	@Override
	public CompletableFuture<Void> onEnd() {
		// The response has to be served yet, and it should happen with this call.
		return this.handler.onRequestEnd(currentRequest).thenAccept(x -> currentRequest = null);
	}
	
	@Override
	public void onError(Throwable exc) {
		if (currentRequest != null) {
			handler.onRequestError(currentRequest, exc);
		}
	}

}
