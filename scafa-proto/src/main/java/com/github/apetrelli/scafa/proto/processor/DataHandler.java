package com.github.apetrelli.scafa.proto.processor;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface DataHandler extends Handler {

	CompletableFuture<Void> onData(ByteBuffer buffer);
}
