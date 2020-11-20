package com.github.apetrelli.scafa.http.server.sync.impl;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.sync.HttpServerHandlerFactory;
import com.github.apetrelli.scafa.http.sync.HttpHandler;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.aio.HandlerFactory;

public class HttpServerHandlerAdapterFactory implements HandlerFactory<HttpHandler, HttpSyncSocket<HttpResponse>> {

	private HttpServerHandlerFactory factory;

	public HttpServerHandlerAdapterFactory(HttpServerHandlerFactory factory) {
		this.factory = factory;
	}

	@Override
	public HttpHandler create(HttpSyncSocket<HttpResponse> sourceChannel) {
		return new HttpServerHandlerAdapter(factory.create(sourceChannel));
	}

}
