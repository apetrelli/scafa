package com.github.apetrelli.scafa.http.server;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public interface HttpServerHandlerFactory {

	HttpServerHandler create(AsyncSocket channel);
}
