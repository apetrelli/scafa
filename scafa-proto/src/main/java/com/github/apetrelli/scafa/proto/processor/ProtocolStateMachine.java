package com.github.apetrelli.scafa.proto.processor;

import java.util.concurrent.CompletableFuture;

public interface ProtocolStateMachine<H, P extends ProcessingContext<?>> {

	CompletableFuture<Void> out(P context, H handler);
}
