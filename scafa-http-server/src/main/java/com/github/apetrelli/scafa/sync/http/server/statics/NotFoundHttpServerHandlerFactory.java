package com.github.apetrelli.scafa.sync.http.server.statics;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.server.HttpServer;
import com.github.apetrelli.scafa.sync.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.sync.http.server.HttpServerHandlerFactory;

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
