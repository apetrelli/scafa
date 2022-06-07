package com.github.apetrelli.scafa.http.gateway.sync.handler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.sync.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class DefaultGatewayHttpHandlerFactory<T extends HttpSyncSocket<HttpRequest>> implements HandlerFactory<HttpHandler, SyncSocket> {

	private GatewayHttpConnectionFactoryFactory<T> factory;

	public DefaultGatewayHttpHandlerFactory(GatewayHttpConnectionFactoryFactory<T> factory) {
		this.factory = factory;
	}

	@Override
	public HttpHandler create(SyncSocket sourceChannel) {
		return new DefaultGatewayHttpHandler<>(factory.create(), sourceChannel);
	}

}
