package com.github.apetrelli.scafa.http.server;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpRequest;

public interface HttpServerHandler {

	void onStart();

	CompletableFuture<Void> onRequestHeader(HttpRequest request);

	CompletableFuture<Void> onBody(HttpRequest request, ByteBuffer buffer, long offset, long length);

	CompletableFuture<Void> onRequestEnd(HttpRequest request);

	CompletableFuture<Void> onRequestError(HttpRequest request, Throwable exc);

}
