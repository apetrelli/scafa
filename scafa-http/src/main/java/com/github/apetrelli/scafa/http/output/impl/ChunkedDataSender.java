package com.github.apetrelli.scafa.http.output.impl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.async.AsyncSocket;

public class ChunkedDataSender extends AbstractDataSender {

	private static final String END_OF_CHUNKED_TRANSFER_SIZE_STRING = "0";

	private static final byte CR = 13;

	private static final byte LF = 10;

	public ChunkedDataSender(AsyncSocket channel) {
		super(channel);
	}
	
	@Override
	public CompletableFuture<Void> send(ByteBuffer buffer) {
		return sendChunkSize(buffer.remaining()).thenCompose(x -> channel.flushBuffer(buffer))
				.thenCompose(x -> sendNewline());
	}
	
	@Override
	public CompletableFuture<Void> end() {
		return sendEndOfChunkedTransfer();
	}

	private CompletableFuture<Void> sendChunkSize(long size) {
		String sizeString = Long.toString(size, 16);
		ByteBuffer buffer = ByteBuffer.allocate(sizeString.length() + 2);
		buffer.put(sizeString.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF);
		return channel.flipAndFlushBuffer(buffer);
	}

	private CompletableFuture<Void> sendNewline() {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.put(CR).put(LF);
		return channel.flipAndFlushBuffer(buffer);
	}

	public CompletableFuture<Void> sendEndOfChunkedTransfer() {
		ByteBuffer buffer = ByteBuffer.allocate(END_OF_CHUNKED_TRANSFER_SIZE_STRING.length() + 4);
		buffer.put(END_OF_CHUNKED_TRANSFER_SIZE_STRING.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF).put(CR)
				.put(LF);
		return channel.flipAndFlushBuffer(buffer);
	}
}
