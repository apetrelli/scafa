package com.github.apetrelli.scafa.proto.processor;

import java.nio.ByteBuffer;

public class ProcessingContext<ST> implements Input {

	private ByteBuffer buffer;

	private ST status;

	public ProcessingContext(ST status) {
		this.status = status;
	}

	public ST getStatus() {
		return status;
	}

	public void setStatus(ST status) {
		this.status = status;
	}

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
