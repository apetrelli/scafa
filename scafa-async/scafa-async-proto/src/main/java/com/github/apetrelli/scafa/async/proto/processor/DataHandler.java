package com.github.apetrelli.scafa.async.proto.processor;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.processor.Handler;

public interface DataHandler extends Handler {

	CompletableFuture<Void> onData(ByteBuffer buffer);
}
