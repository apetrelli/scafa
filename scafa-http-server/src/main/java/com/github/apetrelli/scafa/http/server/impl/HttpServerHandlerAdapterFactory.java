package com.github.apetrelli.scafa.http.server.impl;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.server.HttpServerHandlerFactory;
import com.github.apetrelli.scafa.proto.aio.HandlerFactory;

public class HttpServerHandlerAdapterFactory implements HandlerFactory<HttpHandler, HttpAsyncSocket> {

	private HttpServerHandlerFactory factory;

	public HttpServerHandlerAdapterFactory(HttpServerHandlerFactory factory) {
		this.factory = factory;
	}

	@Override
	public HttpHandler create(HttpAsyncSocket sourceChannel) {
		return new HttpServerHandlerAdapter(factory.create(sourceChannel));
	}

}
