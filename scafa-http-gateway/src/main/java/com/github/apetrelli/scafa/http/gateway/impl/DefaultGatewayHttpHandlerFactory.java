package com.github.apetrelli.scafa.http.gateway.impl;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.proto.aio.HandlerFactory;

public class DefaultGatewayHttpHandlerFactory implements HandlerFactory<HttpHandler> {

	private GatewayHttpConnectionFactoryFactory factory;

	public DefaultGatewayHttpHandlerFactory(GatewayHttpConnectionFactoryFactory factory) {
		this.factory = factory;
	}

	@Override
	public HttpHandler create(AsynchronousSocketChannel sourceChannel) {
		return new DefaultGatewayHttpHandler(factory.create(), sourceChannel);
	}

}
