package com.github.apetrelli.scafa.http.server;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;

public interface HttpServerHandlerFactory {

	HttpServerHandler create(HttpAsyncSocket<HttpResponse> channel);
}
