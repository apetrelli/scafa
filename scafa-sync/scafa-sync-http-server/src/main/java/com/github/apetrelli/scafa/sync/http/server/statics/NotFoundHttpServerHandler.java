package com.github.apetrelli.scafa.sync.http.server.statics;

import static com.github.apetrelli.scafa.http.HttpCodes.METHOD_NOT_ALLOWED;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH_0;
import static com.github.apetrelli.scafa.http.HttpHeaders.GET;
import static com.github.apetrelli.scafa.http.HttpHeaders.HTTP_1_1;
import static com.github.apetrelli.scafa.http.HttpHeaders.SCAFA;
import static com.github.apetrelli.scafa.http.HttpHeaders.SERVER;

import com.github.apetrelli.scafa.http.HttpCodes;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.server.HttpServer;
import com.github.apetrelli.scafa.sync.http.server.impl.HttpServerHandlerSupport;
import com.github.apetrelli.scafa.proto.util.AsciiString;


public class NotFoundHttpServerHandler extends HttpServerHandlerSupport {
	
	private static final AsciiString ONLY_GET_ALLOWED = new AsciiString("Only GET allowed");

	private final HttpServer server;

	public NotFoundHttpServerHandler(HttpSyncSocket<HttpResponse> channel, HttpServer server) {
		super(channel);
		this.server = server;
	}
	
	@Override
	public void onRequestEnd(HttpRequest request) {
		if (GET.equals(request.getMethod())) {
			sendSimpleMessage(HttpCodes.NOT_FOUND, new AsciiString("Resource " + request.getResource() +  " not found"));
		} else {
			sendSimpleMessage(METHOD_NOT_ALLOWED, ONLY_GET_ALLOWED);
		}
	}

	private void sendSimpleMessage(AsciiString httpCode, AsciiString message) {
		HttpResponse response = new HttpResponse(HTTP_1_1, httpCode, message);
		response.headers().setHeader(SERVER, SCAFA);
		response.headers().setHeader(CONTENT_LENGTH, CONTENT_LENGTH_0);
		server.response(channel, response);
	}
}
