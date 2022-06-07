package com.github.apetrelli.scafa.sync.http.output.impl;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.sync.http.output.DataSender;

public class NullDataSender implements DataSender {

	@Override
	public void send(ByteBuffer buffer) {
		// Discard all the data.
		buffer.position(buffer.limit());
	}

	@Override
	public void end() {
		// Does nothing.
	}
}
