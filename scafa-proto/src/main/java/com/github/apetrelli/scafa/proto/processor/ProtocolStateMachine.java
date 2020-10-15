package com.github.apetrelli.scafa.proto.processor;

import java.util.concurrent.CompletableFuture;

public interface ProtocolStateMachine<H, ST, P extends ProcessingContext<ST>> {

	ST next(P context);

	CompletableFuture<Void> out(P context, H handler);
}
