package com.github.apetrelli.scafa.http.server;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;

public interface HttpServerHandlerFactory {

	HttpServerHandler create(HttpAsyncSocket channel);
}
