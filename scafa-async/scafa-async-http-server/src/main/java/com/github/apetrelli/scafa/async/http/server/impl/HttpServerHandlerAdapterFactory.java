package com.github.apetrelli.scafa.async.http.server.impl;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.http.server.HttpServerHandlerFactory;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpServerHandlerAdapterFactory implements HandlerFactory<HttpHandler, HttpAsyncSocket<HttpResponse>> {

	private final HttpServerHandlerFactory factory;

	@Override
	public HttpHandler create(HttpAsyncSocket<HttpResponse> sourceChannel) {
		return new HttpServerHandlerAdapter(factory.create(sourceChannel));
	}

}
