package com.github.apetrelli.scafa.http.server;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpResponse;

public interface HttpServerHandlerFactory {

	HttpServerHandler create(HttpAsyncSocket<HttpResponse> channel);
}
