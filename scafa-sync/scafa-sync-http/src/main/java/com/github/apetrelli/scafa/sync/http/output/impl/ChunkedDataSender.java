package com.github.apetrelli.scafa.sync.http.output.impl;

import java.nio.charset.StandardCharsets;

import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.proto.io.OutputFlow;

public class ChunkedDataSender extends AbstractDataSender {

	private static final byte ZERO = 48;

	private static final byte CR = 13;

	private static final byte LF = 10;

	public ChunkedDataSender(Socket channel) {
		super(channel);
	}
	
	@Override
	public void send(FlowBuffer buffer) {
		OutputFlow out = channel.out();
		sendChunkSize(out, buffer.length());
		out.write(buffer);
		sendNewline(out);
		out.flush();
	}
	
	@Override
	public void end() {
		sendEndOfChunkedTransfer(channel.out());
	}

	private void sendChunkSize(OutputFlow out, long size) {
		String sizeString = Long.toString(size, 16);
		out.write(sizeString.getBytes(StandardCharsets.US_ASCII)).write(CR).write(LF).flush();
	}

	private void sendNewline(OutputFlow out) {
		out.write(CR).write(LF);
	}

	public void sendEndOfChunkedTransfer(OutputFlow out) {
		out.write(ZERO).write(CR).write(LF).write(CR).write(LF).flush();
	}
}
