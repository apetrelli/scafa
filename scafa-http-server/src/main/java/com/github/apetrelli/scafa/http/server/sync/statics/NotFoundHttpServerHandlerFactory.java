package com.github.apetrelli.scafa.http.server.sync.statics;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.sync.HttpServer;
import com.github.apetrelli.scafa.http.server.sync.HttpServerHandler;
import com.github.apetrelli.scafa.http.server.sync.HttpServerHandlerFactory;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;

public class NotFoundHttpServerHandlerFactory implements HttpServerHandlerFactory {

	private HttpServer server;

	public NotFoundHttpServerHandlerFactory(HttpServer server) {
		this.server = server;
	}

	@Override
	public HttpServerHandler create(HttpSyncSocket<HttpResponse> channel) {
		return new NotFoundHttpServerHandler(channel, server);
	}

}
