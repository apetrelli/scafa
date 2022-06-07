package com.github.apetrelli.scafa.sync.http.output.impl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class ChunkedDataSender extends AbstractDataSender {

	private static final String END_OF_CHUNKED_TRANSFER_SIZE_STRING = "0";

	private static final byte CR = 13;

	private static final byte LF = 10;

	public ChunkedDataSender(SyncSocket channel) {
		super(channel);
	}
	
	@Override
	public void send(ByteBuffer buffer) {
		sendChunkSize(buffer.remaining());
		channel.flushBuffer(buffer);
		sendNewline();
	}
	
	@Override
	public void end() {
		sendEndOfChunkedTransfer();
	}

	private void sendChunkSize(long size) {
		String sizeString = Long.toString(size, 16);
		ByteBuffer buffer = ByteBuffer.allocate(sizeString.length() + 2);
		buffer.put(sizeString.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF);
		channel.flipAndFlushBuffer(buffer);
	}

	private void sendNewline() {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.put(CR).put(LF);
		channel.flipAndFlushBuffer(buffer);
	}

	public void sendEndOfChunkedTransfer() {
		ByteBuffer buffer = ByteBuffer.allocate(END_OF_CHUNKED_TRANSFER_SIZE_STRING.length() + 4);
		buffer.put(END_OF_CHUNKED_TRANSFER_SIZE_STRING.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF).put(CR)
				.put(LF);
		channel.flipAndFlushBuffer(buffer);
	}
}
