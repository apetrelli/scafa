package com.github.apetrelli.scafa.proto.processor;

public interface ProtocolStateMachine<H, ST, P extends ProcessingContext<ST>> {

	ST next(byte ch, P context);

	void out(byte ch, P context, H handler);
}
