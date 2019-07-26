package com.github.apetrelli.scafa.proto.processor;

import java.nio.channels.CompletionHandler;

public interface ProtocolStateMachine<I extends Input, S extends ByteSink<I>, P extends ProcessingContext<I, S>> {

	Status<I, S> next(P context);

	void out(P context, S sink, CompletionHandler<Void, Void> completionHandler);
}
