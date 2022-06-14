package com.github.apetrelli.scafa.async.http.server.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CHUNKED;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.TRANSFER_ENCODING;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.github.apetrelli.scafa.async.file.BufferContext;
import com.github.apetrelli.scafa.async.file.BufferContextReader;
import com.github.apetrelli.scafa.async.file.PathBufferContextReader;
import com.github.apetrelli.scafa.async.file.PathBufferContextReaderFactory;
import com.github.apetrelli.scafa.async.file.PathIOException;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.output.DataSender;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.http.server.HttpServer;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.util.AsciiString;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@RequiredArgsConstructor
@Log
public class DefaultHttpServer implements HttpServer {

	private final DataSenderFactory dataSenderFactory;
	
	private final PathBufferContextReaderFactory pathBufferContextReaderFactory;
	
	@Override
	public CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, ByteBuffer writeBuffer) {
		return channel.sendHeader(response, writeBuffer);
	}
	
	@Override
	public CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response,
			BufferContextReader payloadReader, ByteBuffer writeBuffer) {
		response.setHeader(TRANSFER_ENCODING, CHUNKED);
		return responseWithPayload(channel, response, payloadReader, writeBuffer);
	}

	@Override
	public CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response,
			BufferContextReader payloadReader, long size, ByteBuffer writeBuffer) {
		response.setHeader(CONTENT_LENGTH, new AsciiString(Long.toString(size)));
		return responseWithPayload(channel, response, payloadReader, writeBuffer);
	}
	
	@Override
	public CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response,
			Path payload, ByteBuffer writeBuffer) {
		PathBufferContextReader payloadReader = null;
		try {
			PathBufferContextReader currentPayloadReader = pathBufferContextReaderFactory.create(payload);
			payloadReader = currentPayloadReader;
			return response(channel, response, payloadReader, payloadReader.size(), writeBuffer).thenRun(() -> {
				try {
					currentPayloadReader.close();
				} catch (IOException e) {
					log.log(Level.FINE, "Error when closing payload reader", e);
				}
			});
		} catch (PathIOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	private CompletableFuture<Void> responseWithPayload(HttpAsyncSocket<HttpResponse> channel, HttpResponse realResponse,
			BufferContextReader payloadReader, ByteBuffer writeBuffer) {
		DataSender sender = dataSenderFactory.create(realResponse, channel);
		BufferContext fileContext = new BufferContext();
		fileContext.setBuffer(writeBuffer);
		return channel.sendHeader(realResponse, writeBuffer).thenCompose(x -> transferPayload(payloadReader, sender, fileContext));
	}

	private CompletableFuture<Void> transferPayload(BufferContextReader payloadReader, DataSender sender,
			BufferContext fileContext) {
		return payloadReader.read(fileContext).thenCompose(y -> {
			if (y >= 0) {
				return sender.send(fileContext.getBuffer()).thenCompose(x -> {
					fileContext.getBuffer().clear();
					return transferPayload(payloadReader, sender, fileContext);
				});
			} else {
				return sender.end();
			}
		});
	}
}
