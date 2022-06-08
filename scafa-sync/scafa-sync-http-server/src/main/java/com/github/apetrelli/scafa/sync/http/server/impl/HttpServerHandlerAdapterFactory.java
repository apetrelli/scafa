package com.github.apetrelli.scafa.sync.http.server.impl;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.server.HttpServerHandlerFactory;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;

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
