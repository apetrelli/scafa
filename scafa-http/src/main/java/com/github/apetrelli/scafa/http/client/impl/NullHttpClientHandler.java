package com.github.apetrelli.scafa.http.client.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;

public class NullHttpClientHandler implements HttpClientHandler {

	private static final Logger LOG = Logger.getLogger(NullHttpClientHandler.class.getName());

	@Override
	public void onStart(){
		// Do nothing
	}

	@Override
	public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler) {
		handler.completed(null, null);
	}

	@Override
	public void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler) {
		buffer.clear();
		handler.completed(null, null);
	}

	@Override
	public void onEnd() {
		// Do nothing
	}

	@Override
	public void onRequestHeaderSent(HttpRequest request) {
		// Do nothing
	}

	@Override
	public void onRequestError(Throwable exc) {
		LOG.log(Level.SEVERE, "Error when sending the request", exc);
	}

}
