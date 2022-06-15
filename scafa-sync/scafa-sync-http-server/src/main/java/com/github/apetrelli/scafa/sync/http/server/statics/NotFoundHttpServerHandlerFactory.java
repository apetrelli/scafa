package com.github.apetrelli.scafa.sync.http.server.statics;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.server.HttpServer;
import com.github.apetrelli.scafa.sync.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.sync.http.server.HttpServerHandlerFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotFoundHttpServerHandlerFactory implements HttpServerHandlerFactory {

	private final HttpServer server;

	@Override
	public HttpServerHandler create(HttpSyncSocket<HttpResponse> channel) {
		return new NotFoundHttpServerHandler(channel, server);
	}

}
