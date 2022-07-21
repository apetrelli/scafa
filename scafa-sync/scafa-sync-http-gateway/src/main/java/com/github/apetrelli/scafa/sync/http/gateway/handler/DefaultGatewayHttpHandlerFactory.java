package com.github.apetrelli.scafa.sync.http.gateway.handler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactoryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultGatewayHttpHandlerFactory<T extends HttpSyncSocket<HttpRequest>> implements HandlerFactory<HttpHandler, Socket> {

	private final GatewayHttpConnectionFactoryFactory<T> factory;

	@Override
	public HttpHandler create(Socket sourceChannel) {
		return new DefaultGatewayHttpHandler<>(factory.create(), sourceChannel);
	}

}
