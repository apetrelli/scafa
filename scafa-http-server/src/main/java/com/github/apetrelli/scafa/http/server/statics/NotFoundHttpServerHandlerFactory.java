package com.github.apetrelli.scafa.http.server.statics;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.http.server.HttpServerHandlerFactory;

public class NotFoundHttpServerHandlerFactory implements HttpServerHandlerFactory {

	private HttpServer server;

	public NotFoundHttpServerHandlerFactory(HttpServer server) {
		this.server = server;
	}

	@Override
	public HttpServerHandler create(HttpAsyncSocket<HttpResponse> channel) {
		return new NotFoundHttpServerHandler(channel, server);
	}

}
