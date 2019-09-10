package com.github.apetrelli.scafa.http.server;

import java.nio.channels.AsynchronousSocketChannel;

public interface HttpServerHandlerFactory {

	HttpServerHandler create(AsynchronousSocketChannel channel);
}
