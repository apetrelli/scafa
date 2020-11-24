package com.github.apetrelli.scafa.http.server.sync.statics;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.sync.HttpServer;
import com.github.apetrelli.scafa.http.server.sync.impl.HttpServerHandlerSupport;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;


public class NotFoundHttpServerHandler extends HttpServerHandlerSupport {

	private HttpServer server;

	public NotFoundHttpServerHandler(HttpSyncSocket<HttpResponse> channel, HttpServer server) {
		super(channel);
		this.server = server;
	}
	
	@Override
	public void onRequestEnd(HttpRequest request) {
		if ("GET".equals(request.getMethod())) {
			sendSimpleMessage(404, "Resource " + request.getResource() +  " not found");
		} else {
			sendSimpleMessage(405, "Only GET allowed");
		}
	}

	private void sendSimpleMessage(int httpCode, String message) {
		HttpResponse response = new HttpResponse("HTTP/1.1", httpCode, message);
		response.setHeader("Server", "Scafa");
		response.setHeader("Content-Length", "0");
		server.response(channel, response);
	}
}
