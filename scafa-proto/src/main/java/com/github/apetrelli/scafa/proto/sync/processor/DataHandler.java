package com.github.apetrelli.scafa.proto.sync.processor;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.proto.processor.Handler;

public interface DataHandler extends Handler {

	void onData(ByteBuffer buffer);
}
