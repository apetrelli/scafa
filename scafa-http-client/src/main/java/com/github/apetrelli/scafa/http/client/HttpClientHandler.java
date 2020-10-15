package com.github.apetrelli.scafa.http.client;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;

public interface HttpClientHandler {

	void onStart();

	void onRequestHeaderSent(HttpRequest request);

	void onRequestEnd(HttpRequest request);

	void onRequestError(HttpRequest request, Throwable exc);

    CompletableFuture<Void> onResponseHeader(HttpRequest request, HttpResponse response);

    CompletableFuture<Void> onBody(HttpRequest request, HttpResponse response, ByteBuffer buffer, long offset, long length);

    CompletableFuture<Void> onEnd(HttpRequest request, HttpResponse response);
}
