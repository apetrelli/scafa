package com.github.apetrelli.scafa.proto.aio;

import java.nio.ByteBuffer;

public class BufferContext {

	private long position = 0L;

	private ByteBuffer buffer;

	public BufferContext() {
		buffer = ByteBuffer.allocate(16384);
	}

	public void moveForwardBy(int bytes) {
		position += bytes;
	}

	public long getPosition() {
		return position;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
}
