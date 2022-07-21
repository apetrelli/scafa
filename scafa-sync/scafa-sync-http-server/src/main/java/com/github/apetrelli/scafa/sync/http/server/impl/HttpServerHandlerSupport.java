package com.github.apetrelli.scafa.sync.http.server.impl;

import static com.github.apetrelli.scafa.http.HttpCodes.INTERNAL_SERVER_ERROR;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_TYPE;
import static com.github.apetrelli.scafa.http.HttpHeaders.HTTP_1_1;
import static com.github.apetrelli.scafa.http.HttpHeaders.SCAFA;
import static com.github.apetrelli.scafa.http.HttpHeaders.SERVER;
import static com.github.apetrelli.scafa.http.HttpHeaders.TEXT_PLAIN;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.proto.util.AsciiString;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.server.HttpServerHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpServerHandlerSupport implements HttpServerHandler {

	private static final AsciiString UNEXPECTED_EXCEPTION = new AsciiString("Unexpected exception");

	protected final HttpSyncSocket<HttpResponse> channel;

	@Override
	public void onStart() {
		// Does nothing
	}
	
	@Override
	public void onRequestHeader(HttpRequest request) {
		// Does nothing
	}

	@Override
	public void onBody(HttpRequest request, FlowBuffer buffer, long offset, long length) {
		// Does nothing
	}

	@Override
	public void onRequestEnd(HttpRequest request) {
		// Does nothing
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
		channel.sendHeader(response);
		channel.sendData(FlowBuffer.wrap(bytes));
	}

}
