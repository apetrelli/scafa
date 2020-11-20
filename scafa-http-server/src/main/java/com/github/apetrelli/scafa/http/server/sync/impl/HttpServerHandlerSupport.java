package com.github.apetrelli.scafa.http.server.sync.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.sync.HttpServerHandler;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;

public class HttpServerHandlerSupport implements HttpServerHandler {

	protected HttpSyncSocket<HttpResponse> channel;

	public HttpServerHandlerSupport(HttpSyncSocket<HttpResponse> channel) {
		this.channel = channel;
	}

	@Override
	public void onStart() {
		// Does nothing
	}
	
	@Override
	public void onRequestHeader(HttpRequest request) {
		// Does nothing
	}

	@Override
	public void onBody(HttpRequest request, ByteBuffer buffer, long offset, long length) {
		buffer.position(buffer.limit());
	}

	@Override
	public void onRequestEnd(HttpRequest request) {
		// Does nothing
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
		channel.sendHeader(response);
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		channel.sendData(buffer);
	}

}
