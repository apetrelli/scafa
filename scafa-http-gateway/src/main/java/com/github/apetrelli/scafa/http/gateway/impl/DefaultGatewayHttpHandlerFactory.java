package com.github.apetrelli.scafa.http.gateway.impl;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.HandlerFactory;

public class DefaultGatewayHttpHandlerFactory implements HandlerFactory<HttpHandler, AsyncSocket> {

	private GatewayHttpConnectionFactoryFactory factory;

	public DefaultGatewayHttpHandlerFactory(GatewayHttpConnectionFactoryFactory factory) {
		this.factory = factory;
	}

	@Override
	public HttpHandler create(AsyncSocket sourceChannel) {
		return new DefaultGatewayHttpHandler(factory.create(), sourceChannel);
	}

}
