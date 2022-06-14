package com.github.apetrelli.scafa.async.http.server.statics;

import com.github.apetrelli.scafa.http.HttpResponse;

import lombok.RequiredArgsConstructor;

import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.server.HttpServer;
import com.github.apetrelli.scafa.async.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.async.http.server.HttpServerHandlerFactory;

@RequiredArgsConstructor
public class NotFoundHttpServerHandlerFactory implements HttpServerHandlerFactory {

	private final HttpServer server;

	@Override
	public HttpServerHandler create(HttpAsyncSocket<HttpResponse> channel) {
		return new NotFoundHttpServerHandler(channel, server);
	}

}
