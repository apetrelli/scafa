package com.github.apetrelli.scafa.http.output.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public class ChunkedDataSender extends AbstractDataSender {

	private static final String END_OF_CHUNKED_TRANSFER_SIZE_STRING = "0";

	private static final byte CR = 13;

	private static final byte LF = 10;

	public ChunkedDataSender(AsyncSocket channel) {
		super(channel);
	}

	@Override
	public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
		sendChunkSize(buffer.remaining(), new CompletionHandler<Void, Void>() {

			@Override
			public void completed(Void result, Void attachment) {
				channel.flushBuffer(buffer, new CompletionHandler<Void, Void>() {

					@Override
					public void completed(Void result, Void attachment) {
						sendNewline(completionHandler);
					}

					@Override
					public void failed(Throwable exc, Void attachment) {
						completionHandler.failed(exc, attachment);
					}
				});
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				completionHandler.failed(exc, attachment);
			}
		});
	}

	@Override
	public void end(CompletionHandler<Void, Void> completionHandler) {
		sendEndOfChunkedTransfer(completionHandler);
	}

	private void sendChunkSize(long size, CompletionHandler<Void, Void> completionHandler) {
		String sizeString = Long.toString(size, 16);
		ByteBuffer buffer = ByteBuffer.allocate(sizeString.length() + 2);
		buffer.put(sizeString.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF);
		channel.flipAndFlushBuffer(buffer, completionHandler);
	}

	private void sendNewline(CompletionHandler<Void, Void> completionHandler) {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.put(CR).put(LF);
		channel.flipAndFlushBuffer(buffer, completionHandler);
	}

	public void sendEndOfChunkedTransfer(CompletionHandler<Void, Void> completionHandler) {
		ByteBuffer buffer = ByteBuffer.allocate(END_OF_CHUNKED_TRANSFER_SIZE_STRING.length() + 4);
		buffer.put(END_OF_CHUNKED_TRANSFER_SIZE_STRING.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF).put(CR)
				.put(LF);
		channel.flipAndFlushBuffer(buffer, completionHandler);
	}
}
