package com.github.apetrelli.scafa.http.client.impl;

import java.nio.ByteBuffer;

public class FileContext {

	private long position = 0L;

	private ByteBuffer buffer;

	public FileContext() {
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
