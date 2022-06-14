package com.github.apetrelli.scafa.http.client.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.github.apetrelli.scafa.async.proto.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;

import lombok.extern.java.Log;

@Log
public class NullHttpClientHandler implements HttpClientHandler {

	@Override
	public void onStart(){
		// Do nothing
	}

	@Override
	public CompletableFuture<Void> onResponseHeader(HttpRequest request, HttpResponse response) {
		return CompletionHandlerFuture.completeEmpty();
	}
	
	@Override
	public CompletableFuture<Void> onBody(HttpRequest request, HttpResponse response, ByteBuffer buffer, long offset,
			long length) {
		buffer.clear();
		return CompletionHandlerFuture.completeEmpty();
	}
	
	@Override
	public CompletableFuture<Void> onEnd(HttpRequest request, HttpResponse response) {
		return CompletionHandlerFuture.completeEmpty();
	}

	@Override
	public void onRequestHeaderSent(HttpRequest request) {
		// Do nothing
	}

	@Override
	public void onRequestEnd(HttpRequest request) {
		// Do nothing
	}

	@Override
	public void onRequestError(HttpRequest request, Throwable exc) {
		log.log(Level.SEVERE, "Error when sending the request", exc);
	}

}
