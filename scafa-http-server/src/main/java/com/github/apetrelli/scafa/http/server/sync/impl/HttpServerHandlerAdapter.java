package com.github.apetrelli.scafa.http.server.sync.impl;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.sync.HttpServerHandler;
import com.github.apetrelli.scafa.http.sync.HttpHandler;
import com.github.apetrelli.scafa.http.sync.impl.HttpHandlerSupport;


public class HttpServerHandlerAdapter extends HttpHandlerSupport implements HttpHandler {

	private HttpServerHandler handler;

	private HttpRequest currentRequest;

	public HttpServerHandlerAdapter(HttpServerHandler handler) {
		this.handler = handler;
	}

	@Override
	public void onStart() {
		handler.onStart();
	}
	
	@Override
	public void onResponseHeader(HttpResponse response) {
        throw new UnsupportedOperationException("This is for requests only");
	}
	
	@Override
	public void onRequestHeader(HttpRequest request) {
		currentRequest = request;
		handler.onRequestHeader(request);
	}
	
	@Override
	public void onBody(ByteBuffer buffer, long offset, long length) {
		handler.onBody(currentRequest, buffer, offset, length);
	}

	@Override
	public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
		handler.onBody(currentRequest, buffer, totalOffset, -1);
	}
	
	@Override
	public void onDataToPassAlong(ByteBuffer buffer) {
        throw new UnsupportedOperationException("CONNECT method not supported");
	}

	@Override
	public void onEnd() {
		// The response has to be served yet, and it should happen with this call.
		handler.onRequestEnd(currentRequest);
		currentRequest = null;
	}
	
	@Override
	public void onError(Throwable exc) {
		if (currentRequest != null) {
			handler.onRequestError(currentRequest, exc);
		}
	}

}
