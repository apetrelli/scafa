package com.github.apetrelli.scafa.http.server;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;

public interface HttpServerHandler {

	void onStart();

	void onRequestHeader(HttpRequest request);

    void onBody(HttpRequest request, ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler);

	void onRequestEnd(HttpRequest request);

	void onRequestError(HttpRequest request, Throwable exc);

    void onResponseHeaderSent(HttpRequest request, HttpResponse response, CompletionHandler<Void, Void> handler);

	void onEnd(HttpRequest request, HttpResponse response);

}
