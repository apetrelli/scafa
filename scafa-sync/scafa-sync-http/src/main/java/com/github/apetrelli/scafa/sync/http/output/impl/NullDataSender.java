package com.github.apetrelli.scafa.sync.http.output.impl;

import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.sync.http.output.DataSender;

public class NullDataSender implements DataSender {

	@Override
	public void send(FlowBuffer buffer) {
		// Discard all the data.
	}

	@Override
	public void end() {
		// Does nothing.
	}
}
