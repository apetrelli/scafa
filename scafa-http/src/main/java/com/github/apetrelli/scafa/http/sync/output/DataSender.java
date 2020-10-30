package com.github.apetrelli.scafa.http.sync.output;

import java.nio.ByteBuffer;

public interface DataSender {

	void send(ByteBuffer buffer);

	void end();
}
