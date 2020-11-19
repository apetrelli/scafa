package com.github.apetrelli.scafa.http.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;

public class HttpServerHandlerSupport implements HttpServerHandler {

	protected HttpAsyncSocket<HttpResponse> channel;

	public HttpServerHandlerSupport(HttpAsyncSocket<HttpResponse> channel) {
		this.channel = channel;
	}

	@Override
	public void onStart() {
		// Does nothing
	}
	
	@Override
	public CompletableFuture<Void> onRequestHeader(HttpRequest request) {
		return CompletionHandlerFuture.completeEmpty();
	}

	@Override
	public CompletableFuture<Void> onBody(HttpRequest request, ByteBuffer buffer, long offset, long length) {
		buffer.position(buffer.limit());
		return CompletionHandlerFuture.completeEmpty();
	}

	@Override
	public CompletableFuture<Void> onRequestEnd(HttpRequest request) {
		return CompletionHandlerFuture.completeEmpty();
	}

	@Override
	public CompletableFuture<Void> onRequestError(HttpRequest request, Throwable exc) {
		HttpResponse response = new HttpResponse("HTTP/1.1", 500, "Unexpected exception");
		response.setHeader("Server", "Scafa");
		response.setHeader("Content-Type", "text/plain");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (PrintStream ps = new PrintStream(baos)) {
			exc.printStackTrace(ps);
		}
		byte[] bytes = baos.toByteArray();
		response.addHeader("Content-Length", Integer.toString(bytes.length));
		return channel.sendHeader(response).thenCompose(x -> {
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			return channel.sendData(buffer);
		});
	}

}
