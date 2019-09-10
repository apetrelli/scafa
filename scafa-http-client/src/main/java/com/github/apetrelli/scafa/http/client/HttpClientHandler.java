package com.github.apetrelli.scafa.http.client;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;

public interface HttpClientHandler {

	void onStart();

	void onRequestHeaderSent(HttpRequest request);

	void onRequestEnd(HttpRequest request);

	void onRequestError(HttpRequest request, Throwable exc);

    void onResponseHeader(HttpRequest request, HttpResponse response, CompletionHandler<Void, Void> handler);

    void onBody(HttpRequest request, HttpResponse response, ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler);

	void onEnd(HttpRequest request, HttpResponse response, CompletionHandler<Void, Void> handler);
}
