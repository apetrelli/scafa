package com.github.apetrelli.scafa.async.http.server;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;

public interface HttpServerHandlerFactory {

	HttpServerHandler create(HttpAsyncSocket<HttpResponse> channel);
}
