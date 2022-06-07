package com.github.apetrelli.scafa.sync.http.output;

import java.nio.ByteBuffer;

public interface DataSender {

	void send(ByteBuffer buffer);

	void end();
}
