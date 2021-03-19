package com.github.apetrelli.scafa.sync.proto.processor;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.proto.processor.Handler;

public interface DataHandler extends Handler {

	void onData(ByteBuffer buffer);
}
