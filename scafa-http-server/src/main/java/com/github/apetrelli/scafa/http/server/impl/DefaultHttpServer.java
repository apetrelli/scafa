package com.github.apetrelli.scafa.http.server.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.output.DataSender;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.proto.aio.BufferContext;
import com.github.apetrelli.scafa.proto.aio.BufferContextReader;
import com.github.apetrelli.scafa.proto.aio.impl.PathBufferContextReader;
import com.github.apetrelli.scafa.tls.util.IOUtils;

public class DefaultHttpServer implements HttpServer {

	private DataSenderFactory dataSenderFactory;

	public DefaultHttpServer(DataSenderFactory dataSenderFactory) {
		this.dataSenderFactory = dataSenderFactory;
	}
	
	@Override
	public CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response) {
		return channel.sendHeader(response);
	}
	
	@Override
	public CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response,
			BufferContextReader payloadReader) {
		HttpResponse realResponse = new HttpResponse(response);
		realResponse.setHeader("Transfer-Encoding", "chunked");
		return responseWithPayload(channel, realResponse, payloadReader);
	}

	@Override
	public CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response,
			BufferContextReader payloadReader, long size) {
		HttpResponse realResponse = new HttpResponse(response);
		realResponse.setHeader("Content-Length", Long.toString(size));
		return responseWithPayload(channel, realResponse, payloadReader);
	}
	
	@Override
	public CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response,
			Path payload) {
		AsynchronousFileChannel fileChannel = null;
		try {
			fileChannel = AsynchronousFileChannel.open(payload, StandardOpenOption.READ); // NOSONAR
			PathBufferContextReader payloadReader = new PathBufferContextReader(fileChannel); // NOSONAR
			return response(channel, response, payloadReader, fileChannel.size());
		} catch (IOException e1) {
			IOUtils.closeQuietly(fileChannel);
			return CompletableFuture.failedFuture(e1);
		}
	}

	private CompletableFuture<Void> responseWithPayload(HttpAsyncSocket<HttpResponse> channel, HttpResponse realResponse,
			BufferContextReader payloadReader) {
		DataSender sender = dataSenderFactory.create(realResponse, channel);
		BufferContext fileContext = new BufferContext();
		return channel.sendHeader(realResponse).thenCompose(x -> transferPayload(payloadReader, sender, fileContext));
	}

	private CompletableFuture<Void> transferPayload(BufferContextReader payloadReader, DataSender sender,
			BufferContext fileContext) {
		return payloadReader.read(fileContext).thenCompose(y -> {
			if (y.getResult() >= 0) {
				return sender.send(y.getAttachment().getBuffer()).thenCompose(x -> {
					fileContext.getBuffer().clear();
					return transferPayload(payloadReader, sender, fileContext);
				});
			} else {
				return sender.end();
			}
		});
	}
}
