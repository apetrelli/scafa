package com.github.apetrelli.scafa.proto.data.impl;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.proto.data.Input;

public class SimpleInput implements Input {

	protected ByteBuffer buffer;

	@Override
	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

}
