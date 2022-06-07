package com.github.apetrelli.scafa.async.http.gateway.handler;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.http.gateway.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;

public class DefaultGatewayHttpHandlerFactory<T extends HttpAsyncSocket<HttpRequest>> implements HandlerFactory<HttpHandler, AsyncSocket> {

	private GatewayHttpConnectionFactoryFactory<T> factory;

	public DefaultGatewayHttpHandlerFactory(GatewayHttpConnectionFactoryFactory<T> factory) {
		this.factory = factory;
	}

	@Override
	public HttpHandler create(AsyncSocket sourceChannel) {
		return new DefaultGatewayHttpHandler<>(factory.create(), sourceChannel);
	}

}
