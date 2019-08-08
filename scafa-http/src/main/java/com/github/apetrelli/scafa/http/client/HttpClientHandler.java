package com.github.apetrelli.scafa.http.client;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;

public interface HttpClientHandler {

	void onStart();

	void onRequestHeaderSent(HttpRequest request);

	void onRequestError(Throwable exc);

    void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler);

    void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler);

	void onEnd();
}
