package com.github.apetrelli.scafa.proto.processor;

import java.nio.ByteBuffer;

public class SimpleInput implements Input {

	private ByteBuffer buffer;

	@Override
	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public byte peekNextByte() {
        return buffer.array()[buffer.position()];
	}

}
