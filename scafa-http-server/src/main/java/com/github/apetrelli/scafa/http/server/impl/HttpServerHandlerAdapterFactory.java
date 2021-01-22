package com.github.apetrelli.scafa.http.server.impl;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.async.HttpHandler;
import com.github.apetrelli.scafa.http.server.HttpServerHandlerFactory;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;

public class HttpServerHandlerAdapterFactory implements HandlerFactory<HttpHandler, HttpAsyncSocket<HttpResponse>> {

	private HttpServerHandlerFactory factory;

	public HttpServerHandlerAdapterFactory(HttpServerHandlerFactory factory) {
		this.factory = factory;
	}

	@Override
	public HttpHandler create(HttpAsyncSocket<HttpResponse> sourceChannel) {
		return new HttpServerHandlerAdapter(factory.create(sourceChannel));
	}

}
