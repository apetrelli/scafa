package com.github.apetrelli.scafa.async.http.server.statics;

import static com.github.apetrelli.scafa.http.HttpCodes.METHOD_NOT_ALLOWED;
import static com.github.apetrelli.scafa.http.HttpCodes.NOT_FOUND;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH_0;
import static com.github.apetrelli.scafa.http.HttpHeaders.GET;
import static com.github.apetrelli.scafa.http.HttpHeaders.HTTP_1_1;
import static com.github.apetrelli.scafa.http.HttpHeaders.SCAFA;
import static com.github.apetrelli.scafa.http.HttpHeaders.SERVER;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.server.HttpServer;
import com.github.apetrelli.scafa.async.http.server.impl.HttpServerHandlerSupport;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class NotFoundHttpServerHandler extends HttpServerHandlerSupport {
	
	private static final AsciiString ONLY_GET_ALLOWED = new AsciiString("Only GET allowed");
	
	private final HttpServer server;

	public NotFoundHttpServerHandler(HttpAsyncSocket<HttpResponse> channel, HttpServer server) {
		super(channel);
		this.server = server;
	}
	
	@Override
	public CompletableFuture<Void> onRequestEnd(HttpRequest request) {
		if (GET.equals(request.getMethod())) {
			return sendSimpleMessage(NOT_FOUND, "Resource " + request.getResource() +  " not found");
		} else {
			return sendSimpleMessage(METHOD_NOT_ALLOWED, ONLY_GET_ALLOWED);
		}
	}

	private CompletableFuture<Void> sendSimpleMessage(AsciiString httpCode, String message) {
		return sendSimpleMessage(httpCode, new AsciiString(message));
	}

	private CompletableFuture<Void> sendSimpleMessage(AsciiString httpCode, AsciiString message) {
		HttpResponse response = new HttpResponse(HTTP_1_1, httpCode, message);
		response.setHeader(SERVER, SCAFA);
		response.setHeader(CONTENT_LENGTH, CONTENT_LENGTH_0);
		return server.response(channel, response, writeBuffer);
	}
}
