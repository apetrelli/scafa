package com.github.apetrelli.scafa.sync.http.server;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;

public interface HttpServerHandler {

	void onStart();

	void onRequestHeader(HttpRequest request);

	void onBody(HttpRequest request, FlowBuffer buffer, long offset, long length);

	void onRequestEnd(HttpRequest request);

	void onRequestError(HttpRequest request, Throwable exc);

}
