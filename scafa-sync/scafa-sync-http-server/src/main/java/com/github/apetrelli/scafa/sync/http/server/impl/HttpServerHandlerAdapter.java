package com.github.apetrelli.scafa.sync.http.server.impl;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.impl.HttpHandlerSupport;
import com.github.apetrelli.scafa.sync.http.server.HttpServerHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpServerHandlerAdapter extends HttpHandlerSupport implements HttpHandler {

	private final HttpServerHandler handler;

	private HttpRequest currentRequest;

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
	public void onBody(FlowBuffer buffer, long offset, long length) {
		handler.onBody(currentRequest, buffer, offset, length);
	}

	@Override
	public void onChunk(FlowBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
		handler.onBody(currentRequest, buffer, totalOffset, -1);
	}
	
	@Override
	public void onDataToPassAlong(FlowBuffer buffer) {
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
