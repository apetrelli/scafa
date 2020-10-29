package com.github.apetrelli.scafa.proto.processor;

public interface ProtocolStateMachine<H, P extends ProcessingContext<?>, R> {

	R out(P context, H handler);
}
