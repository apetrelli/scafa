package com.github.apetrelli.scafa.http.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.proto.aio.IgnoringCompletionHandler;

public class HttpServerHandlerSupport implements HttpServerHandler {

	protected HttpAsyncSocket<HttpResponse> channel;

	public HttpServerHandlerSupport(HttpAsyncSocket<HttpResponse> channel) {
		this.channel = channel;
	}

	@Override
	public void onStart() {
	}

	@Override
	public void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler) {
		handler.completed(null, null);
	}

	@Override
	public void onBody(HttpRequest request, ByteBuffer buffer, long offset, long length,
			CompletionHandler<Void, Void> handler) {
		buffer.position(buffer.limit());
		handler.completed(null, null);
	}

	@Override
	public void onRequestEnd(HttpRequest request, CompletionHandler<Void, Void> handler) {
		handler.completed(null, null);
	}

	@Override
	public void onRequestError(HttpRequest request, Throwable exc) {
		HttpResponse response = new HttpResponse("HTTP/1.1", 500, "Unexpected exception");
		response.setHeader("Server", "Scafa");
		response.setHeader("Content-Type", "text/plain");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (PrintStream ps = new PrintStream(baos)) {
			exc.printStackTrace(ps);
		}
		byte[] bytes = baos.toByteArray();
		response.addHeader("Content-Length", Integer.toString(bytes.length));
		channel.sendHeader(response, new CompletionHandler<Void, Void>() {

			@Override
			public void completed(Void result, Void attachment) {
				ByteBuffer buffer = ByteBuffer.wrap(bytes);
				channel.sendData(buffer, new IgnoringCompletionHandler<>());
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				// Ignore
			}
		});
	}

}
