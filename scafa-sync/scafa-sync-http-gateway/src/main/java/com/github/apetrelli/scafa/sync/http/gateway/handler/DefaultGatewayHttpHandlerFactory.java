package com.github.apetrelli.scafa.sync.http.gateway.handler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultGatewayHttpHandlerFactory<T extends HttpSyncSocket<HttpRequest>> implements HandlerFactory<HttpHandler, SyncSocket> {

	private final GatewayHttpConnectionFactoryFactory<T> factory;

	@Override
	public HttpHandler create(SyncSocket sourceChannel) {
		return new DefaultGatewayHttpHandler<>(factory.create(), sourceChannel);
	}

}
