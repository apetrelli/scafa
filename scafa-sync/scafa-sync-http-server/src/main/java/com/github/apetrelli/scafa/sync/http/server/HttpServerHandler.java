package com.github.apetrelli.scafa.sync.http.server;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpRequest;

public interface HttpServerHandler {

	void onStart();

	void onRequestHeader(HttpRequest request);

	void onBody(HttpRequest request, ByteBuffer buffer, long offset, long length);

	void onRequestEnd(HttpRequest request);

	void onRequestError(HttpRequest request, Throwable exc);

}
