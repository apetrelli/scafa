package com.github.apetrelli.scafa.proto.processor;

import java.nio.channels.CompletionHandler;

public interface ProtocolStateMachine<I extends Input, S extends ByteSink<I>, H, ST, P extends ProcessingContext<I, ST>> {

	ST next(P context);

	void out(P context, S sink, CompletionHandler<Void, Void> completionHandler);

	void out(P context, H handler, CompletionHandler<Void, Void> completionHandler);
}
