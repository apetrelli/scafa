package com.github.apetrelli.scafa.async.http.server.impl;

import static com.github.apetrelli.scafa.http.HttpCodes.INTERNAL_SERVER_ERROR;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_TYPE;
import static com.github.apetrelli.scafa.http.HttpHeaders.HTTP_1_1;
import static com.github.apetrelli.scafa.http.HttpHeaders.SCAFA;
import static com.github.apetrelli.scafa.http.HttpHeaders.SERVER;
import static com.github.apetrelli.scafa.http.HttpHeaders.TEXT_PLAIN;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.proto.util.AsciiString;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpServerHandlerSupport implements HttpServerHandler {
	private static final AsciiString UNEXPECTED_EXCEPTION = new AsciiString("Unexpected exception");

	protected final HttpAsyncSocket<HttpResponse> channel;
	
	protected final ByteBuffer writeBuffer = ByteBuffer.allocate(16384);

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
	public void onRequestError(HttpRequest request, Throwable exc) {
		HttpResponse response = new HttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, UNEXPECTED_EXCEPTION);
		response.setHeader(SERVER, SCAFA);
		response.setHeader(CONTENT_TYPE, TEXT_PLAIN);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (PrintStream ps = new PrintStream(baos)) {
			exc.printStackTrace(ps);
		}
		byte[] bytes = baos.toByteArray();
		response.addHeader(CONTENT_LENGTH, new AsciiString(Integer.toString(bytes.length)));
		channel.sendHeader(response, writeBuffer).thenCompose(x -> {
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			return channel.sendData(buffer);
		});
	}

}
