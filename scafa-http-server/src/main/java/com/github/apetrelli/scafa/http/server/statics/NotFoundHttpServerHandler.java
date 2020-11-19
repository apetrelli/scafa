package com.github.apetrelli.scafa.http.server.statics;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.http.server.impl.HttpServerHandlerSupport;

public class NotFoundHttpServerHandler extends HttpServerHandlerSupport {

	private HttpServer server;

	public NotFoundHttpServerHandler(HttpAsyncSocket<HttpResponse> channel, HttpServer server) {
		super(channel);
		this.server = server;
	}
	
	@Override
	public CompletableFuture<Void> onRequestEnd(HttpRequest request) {
		if ("GET".equals(request.getMethod())) {
			return sendSimpleMessage(404, "Resource " + request.getResource() +  " not found");
		} else {
			return sendSimpleMessage(405, "Only GET allowed");
		}
	}

	private CompletableFuture<Void> sendSimpleMessage(int httpCode, String message) {
		HttpResponse response = new HttpResponse("HTTP/1.1", httpCode, message);
		response.setHeader("Server", "Scafa");
		response.setHeader("Content-Length", "0");
		return server.response(channel, response);
	}
}