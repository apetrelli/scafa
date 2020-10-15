package com.github.apetrelli.scafa.proto.processor;

import java.nio.channels.CompletionHandler;

public interface ProtocolStateMachine<H, ST, P extends ProcessingContext<ST>> {

	ST next(P context);

	void out(P context, H handler, CompletionHandler<Void, Void> completionHandler);
}
