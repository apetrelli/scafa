package com.github.apetrelli.scafa.async.http.output;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface DataSender {

	CompletableFuture<Void> send(ByteBuffer buffer);

	CompletableFuture<Void> end();
}
