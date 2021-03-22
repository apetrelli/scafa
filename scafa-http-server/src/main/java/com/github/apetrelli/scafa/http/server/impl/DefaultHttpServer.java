package com.github.apetrelli.scafa.http.server.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CHUNKED;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.TRANSFER_ENCODING;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.async.file.BufferContext;
import com.github.apetrelli.scafa.async.file.BufferContextReader;
import com.github.apetrelli.scafa.async.file.aio.IOUtils;
import com.github.apetrelli.scafa.async.file.aio.PathBufferContextReader;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.async.output.DataSender;
import com.github.apetrelli.scafa.http.async.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class DefaultHttpServer implements HttpServer {
	
	private static final Logger LOG = Logger.getLogger(DefaultHttpServer.class.getName());

	private DataSenderFactory dataSenderFactory;

	public DefaultHttpServer(DataSenderFactory dataSenderFactory) {
		this.dataSenderFactory = dataSenderFactory;
	}
	
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
		AsynchronousFileChannel fileChannel = null;
		try {
			fileChannel = AsynchronousFileChannel.open(payload, StandardOpenOption.READ); // NOSONAR
			PathBufferContextReader payloadReader = new PathBufferContextReader(fileChannel); // NOSONAR
			return response(channel, response, payloadReader, fileChannel.size(), writeBuffer).thenRun(() -> {
				try {
					payloadReader.close();
				} catch (IOException e) {
					LOG.log(Level.FINE, "Error when closing payload reader", e);
				}
			});
		} catch (IOException e1) {
			IOUtils.closeQuietly(fileChannel);
			return CompletableFuture.failedFuture(e1);
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
