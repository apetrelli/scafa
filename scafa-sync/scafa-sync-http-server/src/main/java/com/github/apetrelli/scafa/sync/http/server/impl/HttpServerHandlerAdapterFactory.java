package com.github.apetrelli.scafa.sync.http.server.impl;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.server.HttpServerHandlerFactory;

import lombok.RequiredArgsConstructor;

import com.github.apetrelli.scafa.proto.processor.HandlerFactory;

@RequiredArgsConstructor
public class HttpServerHandlerAdapterFactory implements HandlerFactory<HttpHandler, HttpSyncSocket<HttpResponse>> {

	private final HttpServerHandlerFactory factory;

	@Override
	public HttpHandler create(HttpSyncSocket<HttpResponse> sourceChannel) {
		return new HttpServerHandlerAdapter(factory.create(sourceChannel));
	}

}
