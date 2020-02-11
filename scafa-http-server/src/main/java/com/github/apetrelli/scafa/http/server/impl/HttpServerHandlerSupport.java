package com.github.apetrelli.scafa.http.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.http.util.HttpUtils;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.util.AsyncUtils;

public class HttpServerHandlerSupport implements HttpServerHandler {

	protected AsyncSocket channel;

	public HttpServerHandlerSupport(AsyncSocket channel) {
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
		HttpUtils.sendHeader(response, channel, new CompletionHandler<Void, Void>() {

			@Override
			public void completed(Void result, Void attachment) {
				ByteBuffer buffer = ByteBuffer.wrap(bytes);
				AsyncUtils.flushBuffer(buffer, channel, new CompletionHandler<Void, Void>() {

					@Override
					public void completed(Void result, Void attachment) {
						// Ignore
					}

					@Override
					public void failed(Throwable exc, Void attachment) {
						// Ignore
					}
				});
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				// Ignore
			}
		});
	}

}
