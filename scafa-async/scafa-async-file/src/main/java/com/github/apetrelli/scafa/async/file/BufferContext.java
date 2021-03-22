package com.github.apetrelli.scafa.async.file;

import java.nio.ByteBuffer;

public class BufferContext {

	private long position = 0L;

	private ByteBuffer buffer;

	public void moveForwardBy(int bytes) {
		position += bytes;
	}

	public long getPosition() {
		return position;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
	
	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}
}
