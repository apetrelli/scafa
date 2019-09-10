package com.github.apetrelli.scafa.http.server;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;

public interface HttpServerHandler {

	void onStart();

	void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler);

    void onBody(HttpRequest request, ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler);

	void onRequestEnd(HttpRequest request, CompletionHandler<Void, Void> handler);

	void onRequestError(HttpRequest request, Throwable exc);

}
